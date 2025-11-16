package dao;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (first_name, last_name, email, password_hash, phone, user_type, user_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPasswordHash());
            stmt.setString(5, user.getPhone());
            stmt.setString(6, "user");
            stmt.setString(7, "whitelisted");
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        User user = null;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setPhone(rs.getString("phone"));
                user.setUserType(rs.getString("user_type"));
                user.setUserStatus(rs.getString("user_status"));
                user.setVerified(rs.getBoolean("verified"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return user;
    }
    
    // Check if email already exists
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Verify user email
    public boolean verifyUser(String email) {
        String sql = "UPDATE users SET verified = true WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all users for admin panel
    public List<User> getAllUsersForAdmin() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, email, first_name, last_name, phone, user_status FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setPhone(rs.getString("phone"));
                user.setUserStatus(rs.getString("user_status"));
                users.add(user);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    // Update user status (for whitelist/blacklist)
    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET user_status = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Delete user by ID
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users";
        return getCountFromQuery(sql);
    }
    
    public int getWhitelistedUsersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE user_status = 'whitelisted' OR user_status = 'active'";
        return getCountFromQuery(sql);
    }
    
    public int getBlacklistedUsersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE user_status = 'blacklisted' OR user_status = 'inactive'";
        return getCountFromQuery(sql);
    }
    
    private int getCountFromQuery(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Error executing count query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    // Additional method to get users by status for debugging
    public List<User> getUsersByStatus(String status) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, email, first_name, last_name, user_status FROM users WHERE user_status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUserStatus(rs.getString("user_status"));
                users.add(user);
            }
            
            System.out.println("Found " + users.size() + " users with status: " + status);
            
        } catch (SQLException e) {
            System.err.println("Error getting users by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }

    // Method to check actual status values in database for debugging
    public void debugUserStatuses() {
        String sql = "SELECT DISTINCT user_status, COUNT(*) as count FROM users GROUP BY user_status";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("=== USER STATUS DEBUG ===");
            while (rs.next()) {
                String status = rs.getString("user_status");
                int count = rs.getInt("count");
                System.out.println("Status: '" + status + "' - Count: " + count);
            }
            System.out.println("=== END DEBUG ===");
            
        } catch (SQLException e) {
            System.err.println("Error debugging user statuses: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

public User getUserById(int userId) {
    String sql = "SELECT * FROM users WHERE user_id = ?";
    User user = null;
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            user = new User();
            user.setUserId(rs.getInt("user_id"));
            user.setFirstName(rs.getString("first_name"));
            user.setLastName(rs.getString("last_name"));
            user.setEmail(rs.getString("email"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setPhone(rs.getString("phone"));
            user.setUserType(rs.getString("user_type"));
            user.setUserStatus(rs.getString("user_status"));
            user.setVerified(rs.getBoolean("verified"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
        }
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
    
    return user;
}

// Update individual fields
public boolean updateUserFirstName(int userId, String firstName) {
    String sql = "UPDATE users SET first_name = ? WHERE user_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, firstName);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating first name: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

public boolean updateUserLastName(int userId, String lastName) {
    String sql = "UPDATE users SET last_name = ? WHERE user_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, lastName);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating last name: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

public boolean updateUserEmail(int userId, String email) {
    String sql = "UPDATE users SET email = ? WHERE user_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, email);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating email: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

public boolean updateUserPhone(int userId, String phone) {
    String sql = "UPDATE users SET phone = ? WHERE user_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, phone);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating phone: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

// Password update (already exists)
public boolean updateUserPassword(int userId, String newPasswordHash) {
    String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
    
    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, newPasswordHash);
        stmt.setInt(2, userId);
        
        int rowsAffected = stmt.executeUpdate();
        return rowsAffected > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating user password: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}
}