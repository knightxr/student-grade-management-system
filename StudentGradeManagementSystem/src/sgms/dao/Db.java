package sgms.dao;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a connection to our MS Access database using UCanAccess. We build an
 * absolute file path so it works on any computer.
 */
public final class DB {

    private DB() {
        // utility class: do not make objects of this
    }

    /**
     * Make and return a new database connection.
     *
     * @return open JDBC connection
     * @throws SQLException if the connection fails
     */
    public static Connection get() throws SQLException {
        // Make the path absolute so it doesn't depend on the working folder.
        Path db = Path.of("src", "sgms", "data", "School.accdb").toAbsolutePath();

        // Build the UCanAccess URL from the file path.
        String url = "jdbc:ucanaccess://" + db.toString();

        // Connect and return.
        return DriverManager.getConnection(url);
    }
}
