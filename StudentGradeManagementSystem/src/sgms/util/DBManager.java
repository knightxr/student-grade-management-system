package sgms.util;

import java.sql.Connection;
import java.sql.SQLException;

import sgms.dao.Db;

/**
 * @deprecated Use {@link sgms.dao.Db} directly.
 */
@Deprecated
public final class DBManager {

    /** Quick smoke test */
    public static void main(String[] args) {
        try (Connection c = get()) {
            System.out.println("Connected OK");
        } catch (SQLException e) {
            System.err.println("Connection Failed" + e.getMessage());
        }
    }

    /** Call this from legacy code to obtain a live connection */
    public static Connection get() throws SQLException {
        return Db.get();
    }

    private DBManager() { }   // utility class – no instances
}