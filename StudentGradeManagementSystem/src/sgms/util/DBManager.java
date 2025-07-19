package sgms.util;

import java.nio.file.*;
import java.sql.*;

public final class DBManager {

    private static final Path DB_PATH = Paths.get("data", "School.accdb");
    private static final String URL =
        "jdbc:ucanaccess://" + DB_PATH.toAbsolutePath()
        + ";newDatabaseVersion=V2010";   // Auto-create file if absent

    /** Quick smoke test */
    public static void main(String[] args) {
        try (Connection c = get()) {
            System.out.println("Connected OK – " + DB_PATH);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Call this from DAOs to obtain a live connection */
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private DBManager() { }   // utility class – no instances
}