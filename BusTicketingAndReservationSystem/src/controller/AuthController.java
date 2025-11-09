package controller;

import service.AuthService;
import model.User;

public class AuthController {
    private AuthService authService;
    
    public AuthController() {
        this.authService = new AuthService();
    }
    
    public boolean registerUser(String firstName, String lastName, String email, String password, String phone) {
        System.out.println("AuthController: Received registration request");
        
        if (firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            System.out.println("AuthController: Validation failed - empty fields");
            return false;
        }
        
        return authService.registerUser(firstName, lastName, email, password, phone);
    }
    
    public User loginUser(String email, String password, String userType) {
        System.out.println("AuthController: Received login request");
        
        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            System.out.println("AuthController: Validation failed - empty fields");
            return null;
        }
        
        return authService.loginUser(email, password, userType);
    }
    
    public boolean verifyEmail(String email, String otpCode) {
        System.out.println("AuthController: Received OTP verification request");
        
        if (email == null || email.trim().isEmpty() ||
            otpCode == null || otpCode.trim().isEmpty()) {
            System.out.println("AuthController: Validation failed - empty fields");
            return false;
        }
        
        return authService.verifyEmail(email, otpCode);
    }
}