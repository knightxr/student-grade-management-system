package sgms.dao;

public final class DB {
    private static final String URL = "jdbc:ucanaccess://src/sgms/data/School.accdb";

    private DB() {}

    public static java.sql.Connection get() throws java.sql.SQLException {
        return java.sql.DriverManager.getConnection(URL);
    }
}