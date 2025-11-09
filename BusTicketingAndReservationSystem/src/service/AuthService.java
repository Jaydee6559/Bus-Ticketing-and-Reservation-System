package service;

import dao.UserDAO;
import dao.OTPDAO;
import model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthService {
    private UserDAO userDAO;
    private OTPDAO otpDAO;
    private EmailService emailService;
    
    public AuthService() {
        this.userDAO = new UserDAO();
        this.otpDAO = new OTPDAO();
        this.emailService = new EmailService();
    }
    

    public boolean registerUser(String firstName, String lastName, String email, 
                               String password, String phone) {
        System.out.println("AuthService: Starting user registration for " + email);
        
  
        if (userDAO.emailExists(email)) {
            System.out.println("AuthService: Email already exists - " + email);
            return false;
        }
        

        String hashedPassword = hashPassword(password);
        System.out.println("AuthService: Password hashed successfully");
        

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPasswordHash(hashedPassword);
        user.setPhone(phone);
        

        boolean userCreated = userDAO.createUser(user);
        System.out.println("AuthService: User saved to database: " + userCreated);
        
        if (userCreated) {

            String otpCode = generateOTP();
            System.out.println("AuthService: OTP generated: " + otpCode);
            

            boolean otpStored = otpDAO.storeOTP(email, otpCode);
            System.out.println("AuthService: OTP stored in database: " + otpStored);
            
            if (otpStored) {
         
                boolean emailSent = emailService.sendOTPEmail(email, otpCode);
                System.out.println("AuthService: OTP email sent: " + emailSent);
                return emailSent;
            }
        }
        
        return false;
    }
    

    public User loginUser(String email, String password, String userType) {
        System.out.println("AuthService: Attempting login for " + email);
        
 
        User user = userDAO.getUserByEmail(email);
        
        if (user != null) {
            System.out.println("AuthService: User found, checking password and type");
            
            
            String hashedPassword = hashPassword(password);
            boolean passwordCorrect = user.getPasswordHash().equals(hashedPassword);
            
       
            boolean typeCorrect = user.getUserType().equals(userType);
            
            System.out.println("AuthService: Password correct: " + passwordCorrect);
            System.out.println("AuthService: Type correct: " + typeCorrect);
            System.out.println("AuthService: Email verified: " + user.isVerified());
            
            if (passwordCorrect && typeCorrect) {
                return user;
            }
        }
        
        System.out.println("AuthService: Login failed");
        return null;
    }
    
    
    public boolean verifyEmail(String email, String otpCode) {
        System.out.println("AuthService: Verifying OTP for " + email);
        
    
        boolean otpVerified = otpDAO.verifyOTP(email, otpCode);
        System.out.println("AuthService: OTP verified: " + otpVerified);
        
        if (otpVerified) {
         
            boolean userVerified = userDAO.verifyUser(email);
            System.out.println("AuthService: User marked as verified: " + userVerified);
            return userVerified;
        }
        
        return false;
    }
    

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    

    private String generateOTP() {
        // Generate a 6-digit OTP
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}