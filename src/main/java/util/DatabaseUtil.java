package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static final Properties properties = new Properties();
    private static String url;
    private static String username;
    private static String password;

    static {
        loadDatabaseProperties();
    }

    private static void loadDatabaseProperties() {
        try (InputStream input = DatabaseUtil.class
                .getClassLoader()
                .getResourceAsStream("postgres.properties")) {

            if (input == null) {
                throw new RuntimeException("Unable to find postgres.properties file");
            }
            properties.load(input);
            url = properties.getProperty("database.url");
            username = properties.getProperty("database.username");
            password = properties.getProperty("database.password");

        } catch (IOException exception) {
            throw new RuntimeException("Error loading database properties", exception);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
