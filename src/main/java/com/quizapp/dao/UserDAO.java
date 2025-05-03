package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Admin;
import com.quizapp.model.Student;
import com.quizapp.model.User;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticate(String username, String password) throws DatabaseException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String role = rs.getString("role");
                    
                    if ("ADMIN".equals(role)) {
                        return new Admin(id, username, password);
                    } else {
                        return new Student(id, username, password);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error authenticating user: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public void addUser(User user) throws DatabaseException {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding user: " + e.getMessage(), e);
        }
    }
    
    public List<User> getAllUsers() throws DatabaseException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");
                
                if ("ADMIN".equals(role)) {
                    users.add(new Admin(id, username, password));
                } else {
                    users.add(new Student(id, username, password));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving users: " + e.getMessage(), e);
        }
        
        return users;
    }
}
