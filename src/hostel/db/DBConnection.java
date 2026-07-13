package hostel.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * DBConnection — singleton helper for MySQL connectivity.
 * Change DB_URL / USER / PASSWORD to match your MySQL Workbench setup.
 */
public class DBConnection {

    // ── CHANGE THESE IF NEEDED ────────────────────────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/hostel_db?useSSL=false&serverTimezone=UTC";
    private static final String USER    = "root";
    private static final String PASSWORD = "12345";          // ← your MySQL root password
    // ─────────────────────────────────────────────────────────

    private static Connection connection = null;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                "MySQL JDBC Driver not found!\nAdd mysql-connector-java.jar to your build path.",
                "Driver Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to database!\n" + e.getMessage(),
                "DB Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}