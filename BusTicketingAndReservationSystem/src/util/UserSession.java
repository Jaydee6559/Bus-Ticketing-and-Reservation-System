package util;

import model.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;
    
    private UserSession() {
    }
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public String getCurrentUserName() {
        return currentUser != null ? 
            currentUser.getFirstName() + " " + currentUser.getLastName() : "Guest";
    }
    
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
}