package Projects_java.Banking_System.dao;

import Projects_java.Banking_System.model.User;
import Projects_java.Banking_System.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User entity.
 * Handles database operations for user registration, authentication, and retrieval.
 */
public class UserDAO {

    private final Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
        createDefaultAdminIfNotExists();
    }

    /**
     * Registers a new user.
     * Hashes the password with SHA-256 before saving to database.
     */
    public boolean register(User user) throws SQLException {
        if (userExists(user.getEmail())) {
            return false;
        }

        String query = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            // Store hashed password
            ps.setString(3, PasswordUtil.hash(user.getPassword()));
            ps.setString(4, user.getRole() != null ? user.getRole() : "USER");
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * Authenticates user with email and plain password.
     * Supports both SHA-256 hashed passwords and legacy plaintext (with transparent migration).
     */
    public User login(String email, String plainPassword) throws SQLException {
        String query = "SELECT id, full_name, email, password, role, created_at FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    boolean passwordMatches = false;

                    if (PasswordUtil.verify(plainPassword, storedPassword)) {
                        passwordMatches = true;
                    } else if (storedPassword.equals(plainPassword)) {
                        // Legacy plain-text match - upgrade hash automatically
                        passwordMatches = true;
                        upgradePasswordHash(email, plainPassword);
                    }

                    if (passwordMatches) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setPassword(storedPassword);
                        user.setRole(rs.getString("role"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if a user with the given email already exists.
     */
    public boolean userExists(String email) throws SQLException {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Fetches all registered users (used by Admin Panel).
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String query = "SELECT id, full_name, email, role, created_at FROM users ORDER BY id ASC";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(user);
            }
        }
        return list;
    }

    /**
     * Creates default administrator account if one does not already exist.
     */
    private void createDefaultAdminIfNotExists() {
        try {
            if (!userExists("admin@bank.com")) {
                User admin = new User("System Administrator", "admin@bank.com", "Admin@123");
                admin.setRole("ADMIN");
                register(admin);
            }
        } catch (SQLException ignored) {
            // Table might not exist yet if schema has not been executed
        }
    }

    private void upgradePasswordHash(String email, String plainPassword) {
        String updateQuery = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setString(1, PasswordUtil.hash(plainPassword));
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }
}
