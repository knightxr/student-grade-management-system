package sgms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple credential manager backed by the Access database. This implementation
 * is temporary and should be replaced with a more secure solution in
 * production.
 */
public class CredentialManager {

    /**
     * Creates a new {@code CredentialManager}.
     */
    public CredentialManager() {
    }

    /**
     * Validates the provided username and password.
     *
     * @return the username if credentials are correct; otherwise {@code null}
     */
    public String validateLogin(String username, String password) {
        username = username.trim().toLowerCase();
        password = hashPassword(password.trim());
        String sql = "SELECT username FROM tblUsers WHERE username=? AND passwordHash=?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = DBManager.get();
            ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return null;
    }

    /**
     * Checks whether a username already exists in the database.
     */
    public boolean isUsernameExists(String username) {
        username = username.trim().toLowerCase();
        String sql = "SELECT 1 FROM tblUsers WHERE username=?";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = DBManager.get();
            ps = c.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Adds a new user to the database.
     */
    public boolean addUser(String name, String username, String password) {
        username = username.trim().toLowerCase();
        password = password.trim();
        if (isUsernameExists(username)) {
            return false;
        }
        String sql = "INSERT INTO tblUsers (fullName, username, passwordHash) VALUES (?, ?, ?)";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = DBManager.get();
            ps = c.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, username);
            ps.setString(3, hashPassword(password));
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Resets the password for the given username.
     */
    public boolean resetPassword(String username, String newPassword) {
        username = username.trim().toLowerCase();
        newPassword = hashPassword(newPassword.trim());
        String sql = "UPDATE tblUsers SET passwordHash=? WHERE username=?";
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = DBManager.get();
            ps = c.prepareStatement(sql);
            ps.setString(1, newPassword);
            ps.setString(2, username);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Verifies the administrator password against the entry stored in the
     * database. The administrator account uses the fixed username "admin" and
     * the role {@code Administrator}.
     */
    public boolean isAdminPassword(String adminPassword) {
        String sql = "SELECT 1 FROM tblUsers WHERE username=? AND passwordHash=? AND role='Administrator'";
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = DBManager.get();
            ps = c.prepareStatement(sql);
            ps.setString(1, "admin");
            ps.setString(2, hashPassword(adminPassword.trim()));
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ignored) {
            }
            try {
                if (c != null) {
                    c.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    /**
     * Returns a simple hash of the provided password. This implementation uses
     * basic arithmetic so that the hashed value cannot be read directly from
     * the database.
     */
    private static String hashPassword(String password) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            int value = password.charAt(i) * (i + 1) + 11;
            sb.append(Integer.toHexString(value));
        }
        return sb.toString();
    }
}
