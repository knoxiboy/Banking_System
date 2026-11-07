package Projects_java.Banking_System.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class that manages the single JDBC Connection instance.
 *
 * Design Pattern: Singleton — ensures only one database connection
 * is created for the entire application lifecycle, preventing
 * connection leaks and unnecessary resource usage.
 */
public class DBConnection {

    private static Connection connection = null;

    /** Private constructor — prevents external instantiation (Singleton pattern). */
    private DBConnection() {}

    /**
     * Returns the existing Connection, or creates a new one if closed/null.
     *
     * @param url      JDBC connection URL (e.g. jdbc:mysql://localhost:3306/banking_system)
     * @param user     database username
     * @param password database password
     * @return a live {@link Connection} object
     * @throws SQLException if the driver is missing or credentials are wrong
     */
    public static Connection getConnection(String url, String user, String password) throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(url, user, password);
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC Driver not found. Add mysql-connector-j-*.jar to your classpath.\n" + e.getMessage()
                );
            }
        }
        return connection;
    }

    /** Closes the connection cleanly on application shutdown. */
    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
}

