package sgms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import sgms.dao.Db;

/**
 * Simple credential manager backed by the Access database. This implementation
 * is temporary and should be replaced with a more secure solution in
 * production.
 */
public final class CredentialManager {

    private CredentialManager() {
    }

    public static boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.isEmpty()) {
            return false;
        }

        final String sql = "SELECT passwordHash FROM tblUsers WHERE LOWER(username) = LOWER(?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
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

    public static boolean isUsernameExists(String username) {
        if (!isValidUsername(username)) {
            return false;
        }
        String sql = "SELECT 1 FROM tblUsers WHERE username=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addUser(String name, String username, String password) {
        if (!isValidUsername(username) || !isValidPassword(password)) {
            return false;
        }
        String sql = "INSERT INTO tblUsers (fullName, username, passwordHash) VALUES (?, ?, ?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, username.trim().toLowerCase());
            ps.setString(3, hashPassword(password.trim()));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean resetPassword(String username, String newPassword) {
        if (!isValidUsername(username) || !isValidPassword(newPassword)) {
            return false;
        }
        String sql = "UPDATE tblUsers SET passwordHash=? WHERE username=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hashPassword(newPassword.trim()));
            ps.setString(2, username.trim().toLowerCase());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isAdminPassword(String adminPassword) {
        if (!isValidPassword(adminPassword)) {
            return false;
        }
        String sql = "SELECT 1 FROM tblUsers WHERE username=? AND passwordHash=? AND role='Administrator'";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "admin");
            ps.setString(2, hashPassword(adminPassword.trim()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isValidUsername(String s) {
        return s != null && s.matches("[A-Za-z0-9_]{4,20}");
    }

    private static boolean isValidPassword(String s) {
        return s != null && s.length() >= 6;
    }

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
            return true;
        }
        if (stored.equals(plain)) {
            return true;
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
            return s;
        }
    }
}