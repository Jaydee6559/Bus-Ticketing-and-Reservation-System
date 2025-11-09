package model;

import java.sql.Timestamp;

public class OTPVerification {
    private int otpId;
    private String email;
    private String otpCode;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private boolean used;

    // Getters and Setters
    public int getOtpId() { return otpId; }
    public void setOtpId(int otpId) { this.otpId = otpId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
    
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}