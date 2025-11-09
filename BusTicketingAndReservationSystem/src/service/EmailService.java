package service;

public class EmailService {
    
    public boolean sendOTPEmail(String recipientEmail, String otpCode) {
        System.out.println("From: noreply@busticketing.com");

        System.out.println("Body: Your OTP code is: " + otpCode);

        System.out.println("OTP FOR VERIFICATION: " + otpCode);
        
        return true;
    }
}