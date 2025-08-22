package sgms.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;

public final class DB {
    private DB() {}

    /** Cached connection reused across DAO calls. */
    private static Connection shared;

    static {
        System.setProperty("hsqldb.reconfig_logging", "false");
        Logger hsql = Logger.getLogger("org.hsqldb");
        hsql.setLevel(Level.OFF);
        hsql.setUseParentHandlers(false);
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.SEVERE);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(DB::shutdown));
    }

    public static synchronized Connection get() throws SQLException {
        if (shared == null || shared.isClosed()) {
            shared = open();
        }

        InvocationHandler h = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("close".equals(method.getName())) {
                    return null;
                }
                return method.invoke(shared, args);
            }
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                h);
    }

    private static Connection open() throws SQLException {
        Path appDir = Paths.get(System.getProperty("user.home"), ".sgms");
        Path dbPath = appDir.resolve("School.accdb");

        try {
            Files.createDirectories(appDir);
            if (Files.notExists(dbPath) || Files.size(dbPath) == 0) {
                try (InputStream in = DB.class.getResourceAsStream("/sgms/data/School.accdb")) {
                    if (in == null) {
                        throw new IllegalStateException(
                                "Embedded DB not found at /sgms/data/School.accdb inside the JAR");
                    }
                    Files.copy(in, dbPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new SQLException("Failed to prepare DB at " + dbPath, e);
        }

        String url = "jdbc:ucanaccess://" + dbPath
                + ";memory=false;singleConnection=true";
        return DriverManager.getConnection(url);
    }

    public static synchronized void shutdown() {
        if (shared != null) {
            try {
                shared.close();
            } catch (SQLException ignore) {
            }
            shared = null;
        }
    }
}