package sgms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sgms.dao.DB;

/**
 * Very small credential helper that reads/writes users in the Access DB.
 */
public final class CredentialManager {

    private CredentialManager() {
    }

    // SQL (use parameters to avoid SQL injection)
    private static final String SQL_VALIDATE_LOGIN
            = "SELECT passwordHash FROM tblUsers WHERE LOWER(username) = LOWER(?)";

    private static final String SQL_CHECK_USERNAME
            = "SELECT 1 FROM tblUsers WHERE LOWER(username) = LOWER(?)";

    private static final String SQL_ADD_USER
            = "INSERT INTO tblUsers (fullName, username, passwordHash) VALUES (?, ?, ?)";

    private static final String SQL_RESET_PASSWORD
            = "UPDATE tblUsers SET passwordHash=? WHERE LOWER(username)=LOWER(?)";

    private static final String SQL_IS_ADMIN_PASSWORD
            = "SELECT 1 FROM tblUsers WHERE username=? AND passwordHash=? AND role='Administrator'";

    // NEW: fetch the display name
    private static final String SQL_GET_FULL_NAME
            = "SELECT fullName FROM tblUsers WHERE LOWER(username)=LOWER(?)";

    /**
     * Check username + password. Accepts hashed or legacy stored values.
     */
    public static boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }

        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_VALIDATE_LOGIN)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String stored = rs.getString(1);
                return matchesPassword(password, stored);
            }
        } catch (SQLException e) {
            System.err.println("[validateLogin] " + e.getMessage());
            return false;
        }
    }

    /**
     * True if a username already exists (case-insensitive).
     */
    public static boolean isUsernameExists(String username) {
        if (!isValidUsername(username)) {
            return false;
        }
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_CHECK_USERNAME)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[isUsernameExists] " + e.getMessage());
            return false;
        }
    }

    /**
     * Add a user with a simple hash of the password.
     */
    public static boolean addUser(String name, String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password)) {
            return false;
        }
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_ADD_USER)) {
            ps.setString(1, name);
            ps.setString(2, username.trim().toLowerCase()); // store canonical form
            ps.setString(3, hashPassword(password.trim()));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[addUser] " + e.getMessage());
            return false;
        }
    }

    /**
     * Change a user's password (case-insensitive match on username).
     */
    public static boolean resetPassword(String username, String newPassword) {
        if (!isValidUsername(username) || !isValidPassword(newPassword)) {
            return false;
        }
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_RESET_PASSWORD)) {
            ps.setString(1, hashPassword(newPassword.trim()));
            ps.setString(2, username.trim());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            System.err.println("[resetPassword] " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if the given password matches the admin account.
     */
    public static boolean isAdminPassword(String adminPassword) {
        if (adminPassword == null || adminPassword.isEmpty()) {
            return false;
        }
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_IS_ADMIN_PASSWORD)) {
            ps.setString(1, "admin");
            ps.setString(2, hashPassword(adminPassword.trim()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[isAdminPassword] " + e.getMessage());
            return false;
        }
    }

    // ---------- NEW PUBLIC METHOD ----------
    /**
     * Returns the full display name for the given username; falls back to the
     * username if not found.
     */
    public static String getFullNameForUsername(String username) {
        if (username == null || username.isBlank()) {
            return "";
        }
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SQL_GET_FULL_NAME)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String fn = rs.getString(1);
                    if (fn != null && !fn.isBlank()) {
                        return fn.trim();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[getFullNameForUsername] " + e.getMessage());
        }
        return username; // safe fallback
    }

    // ---------- small helpers below ----------
    /**
     * Username: 3–20 letters, digits or underscore.
     */
    private static boolean isValidUsername(String s) {
        return s != null && s.matches("[A-Za-z0-9_]{3,20}");
    }

    /**
     * Password: at least 6 characters.
     */
    private static boolean isValidPassword(String s) {
        return s != null && s.trim().length() >= 6;
    }

    /**
     * VERY simple hash for school use.
     */
    private static String hashPassword(String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            int value = password.charAt(i) * (i + 1) + 11;
            sb.append(Integer.toHexString(value));
        }
        return sb.toString();
    }

    private static boolean matchesPassword(String plain, String stored) {
        if (stored == null) {
            return false;
        }
        if (stored.equals(hashPassword(plain))) {
            return true;  // hashed row
        }
        if (stored.equals(plain)) {
            return true;                // legacy plain
        }
        try {
            String legacy = trySimpleDecode(stored);
            if (plain.equals(legacy)) {
                return true;
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     * Try to decode a legacy base64/xor string.
     */
    private static String trySimpleDecode(String s) {
        try {
            byte[] raw = java.util.Base64.getDecoder().decode(s);
            char key = 0x3A;
            char[] out = new char[raw.length];
            for (int i = 0; i < raw.length; i++) {
                out[i] = (char) (raw[i] ^ key);
            }
            return new String(out);
        } catch (Exception e) {
            return s; // not encoded
        }
    }
}
