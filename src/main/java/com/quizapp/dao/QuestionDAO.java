package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionDAO {
    
    public void addQuestion(Question question) throws DatabaseException {
        String sql = "INSERT INTO questions (text, options, correct_option_index, time_limit) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, question.getText());
            stmt.setString(2, String.join("|", question.getOptions()));
            stmt.setInt(3, question.getCorrectOptionIndex());
            stmt.setInt(4, question.getTimeLimit());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating question failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating question failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding question: " + e.getMessage(), e);
        }
    }
    
    public void updateQuestion(Question question) throws DatabaseException {
        String sql = "UPDATE questions SET text = ?, options = ?, correct_option_index = ?, time_limit = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, question.getText());
            stmt.setString(2, String.join("|", question.getOptions()));
            stmt.setInt(3, question.getCorrectOptionIndex());
            stmt.setInt(4, question.getTimeLimit());
            stmt.setInt(5, question.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Updating question failed, no rows affected.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error updating question: " + e.getMessage(), e);
        }
    }
    
    public Question getQuestionById(int id) throws DatabaseException {
        String sql = "SELECT * FROM questions WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String text = rs.getString("text");
                    String optionsStr = rs.getString("options");
                    List<String> options = Arrays.asList(optionsStr.split("\\|"));
                    int correctOptionIndex = rs.getInt("correct_option_index");
                    int timeLimit = rs.getInt("time_limit");
                    
                    return new Question(id, text, options, correctOptionIndex, timeLimit);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving question: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public List<Question> getAllQuestions() throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM questions";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String text = rs.getString("text");
                String optionsStr = rs.getString("options");
                List<String> options = Arrays.asList(optionsStr.split("\\|"));
                int correctOptionIndex = rs.getInt("correct_option_index");
                int timeLimit = rs.getInt("time_limit");
                
                questions.add(new Question(id, text, options, correctOptionIndex, timeLimit));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving questions: " + e.getMessage(), e);
        }
        
        return questions;
    }
}
