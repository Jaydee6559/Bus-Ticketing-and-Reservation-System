package dao;

import java.sql.*;

public class OTPDAO {
    
    public boolean storeOTP(String email, String otpCode) {
        deleteOTP(email);
        
        String sql = "INSERT INTO otp_verification (email, otp_code, expires_at) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, otpCode);
            
            // Set expiration time (10 minutes from now)
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000);
            stmt.setTimestamp(3, expiresAt);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean verifyOTP(String email, String otpCode) {
        String sql = "SELECT * FROM otp_verification WHERE email = ? AND otp_code = ? AND used = false AND expires_at > NOW()";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.setString(2, otpCode);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Mark OTP as used
                markOTPAsUsed(rs.getInt("otp_id"));
                return true;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private void deleteOTP(String email) {
        String sql = "DELETE FROM otp_verification WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void markOTPAsUsed(int otpId) {
        String sql = "UPDATE otp_verification SET used = true WHERE otp_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, otpId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}