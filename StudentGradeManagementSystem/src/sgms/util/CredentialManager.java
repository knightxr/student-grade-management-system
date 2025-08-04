package sgms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple credential manager backed by the Access database.
 * This implementation is temporary and should be replaced with a more secure
 * solution in production.
 */
public class CredentialManager {

    /** Hard-coded administrator password used for privileged actions. */
    private static final String ADMIN_PASSWORD = "admin";

    /** Creates a new {@code CredentialManager}. */
    public CredentialManager() {}

    /**
     * Validates the provided username and password.
     *
     * @return the username if credentials are correct; otherwise {@code null}
     */
    public String validateLogin(String username, String password) {
        username = username.trim().toLowerCase();
        password = password.trim();
        String sql = "SELECT username FROM tblUsers WHERE username=? AND passwordHash=?";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Checks whether a username already exists in the database. */
    public boolean isUsernameExists(String username) {
        username = username.trim().toLowerCase();
        String sql = "SELECT 1 FROM tblUsers WHERE username=?";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Adds a new user to the database. The {@code name} parameter is reserved
     * for future use and is not persisted.
     */
    public boolean addUser(String name, String username, String password) {
        username = username.trim().toLowerCase();
        password = password.trim();
        if (isUsernameExists(username)) {
            return false;
        }
        String sql = "INSERT INTO tblUsers (username, passwordHash) VALUES (?, ?)";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Resets the password for the given username. */
    public boolean resetPassword(String username, String newPassword) {
        username = username.trim().toLowerCase();
        newPassword = newPassword.trim();
        String sql = "UPDATE tblUsers SET passwordHash=? WHERE username=?";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Verifies the administrator password. */
    public boolean isAdminPassword(String adminPassword) {
        return ADMIN_PASSWORD.equals(adminPassword.trim());
    }
}