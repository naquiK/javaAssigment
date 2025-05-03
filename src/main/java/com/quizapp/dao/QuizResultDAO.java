package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.QuizResult;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResultDAO {
    
    public void addQuizResult(QuizResult result) throws DatabaseException {
        String sql = "INSERT INTO quiz_results (student_id, quiz_id, score, total_questions, completed_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, result.getStudentId());
            stmt.setInt(2, result.getQuizId());
            stmt.setInt(3, result.getScore());
            stmt.setInt(4, result.getTotalQuestions());
            stmt.setTimestamp(5, Timestamp.valueOf(result.getCompletedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz result failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getInt(1));
                } else {
                    throw new DatabaseException("Creating quiz result failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding quiz result: " + e.getMessage(), e);
        }
    }
    
    public List<QuizResult> getResultsByStudentId(int studentId) throws DatabaseException {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results WHERE student_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int quizId = rs.getInt("quiz_id");
                    int score = rs.getInt("score");
                    int totalQuestions = rs.getInt("total_questions");
                    LocalDateTime completedAt = rs.getTimestamp("completed_at").toLocalDateTime();
                    
                    results.add(new QuizResult(id, studentId, quizId, score, totalQuestions, completedAt));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz results: " + e.getMessage(), e);
        }
        
        return results;
    }
    
    public List<QuizResult> getAllResults() throws DatabaseException {
        List<QuizResult> results = new ArrayList<>();
        String sql = "SELECT * FROM quiz_results";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                int studentId = rs.getInt("student_id");
                int quizId = rs.getInt("quiz_id");
                int score = rs.getInt("score");
                int totalQuestions = rs.getInt("total_questions");
                LocalDateTime completedAt = rs.getTimestamp("completed_at").toLocalDateTime();
                
                results.add(new QuizResult(id, studentId, quizId, score, totalQuestions, completedAt));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all quiz results: " + e.getMessage(), e);
        }
        
        return results;
    }
}
