package sgms.util;

import java.sql.Connection;
import java.sql.SQLException;

import sgms.dao.DB;

/**
 * Legacy helper kept so older code still compiles.
 * Use {@link sgms.dao.DB} directly in new code.
 */
@Deprecated
public final class DBManager {

    private DBManager() { } // utility class – no objects

    /** Small smoke test to check the database connection. */
    public static void main(String[] args) {
        try (Connection c = get()) {
            System.out.println("Connected OK");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    /** Returns a live connection. Prefer {@link DB#get()} in new code. */
    public static Connection get() throws SQLException {
        return DB.get();
    }
}
