package sgms.dao;

public final class Db {
    private static final String URL = "jdbc:ucanaccess://C:/Path/To/School.accdb"; // TODO: set real path

    private Db() {}

    public static java.sql.Connection get() throws java.sql.SQLException {
        return java.sql.DriverManager.getConnection(URL);
    }
}