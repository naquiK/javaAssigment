package com.quizapp.service;

import com.quizapp.dao.UserDAO;
import com.quizapp.exception.DatabaseException;
import com.quizapp.model.User;

public class AuthService {
    private UserDAO userDAO;
    private User currentUser;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    public boolean login(String username, String password) {
        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                this.currentUser = user;
                return true;
            }
        } catch (DatabaseException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return false;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equals(currentUser.getRole());
    }
    
    public boolean isStudent() {
        return isLoggedIn() && "STUDENT".equals(currentUser.getRole());
    }
}
