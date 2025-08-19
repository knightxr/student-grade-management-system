package sgms.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DB {
    private DB() {}

    public static Connection get() throws SQLException {
        // 1) Where we'll keep the runtime DB (per-user, writable)
        Path appDir  = Paths.get(System.getProperty("user.home"), ".sgms");
        Path dbPath  = appDir.resolve("School.accdb");

        // 2) Ensure folder exists and extract the embedded DB if missing
        try {
            Files.createDirectories(appDir);
            if (Files.notExists(dbPath) || Files.size(dbPath) == 0) {
                try (InputStream in = DB.class.getResourceAsStream("/sgms/data/School.accdb")) {
                    if (in == null) throw new IllegalStateException(
                        "Embedded DB resource not found at /sgms/data/School.accdb");
                    Files.copy(in, dbPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Failed to prepare database file at " + dbPath, e);
        }

        // 3) Build UCanAccess URL to the real file
        String url = "jdbc:ucanaccess://" + dbPath.toString()
                   + ";memory=false;immediatelyReleaseResources=true";

        return DriverManager.getConnection(url);
    }
}
