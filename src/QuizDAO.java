package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    
    public void addQuiz(Quiz quiz) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insert quiz
            String quizSql = "INSERT INTO quizzes (title) VALUES (?)";
            stmt = conn.prepareStatement(quizSql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, quiz.getTitle());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz failed, no rows affected.");
            }
            
            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                quiz.setId(generatedKeys.getInt(1));
            } else {
                throw new DatabaseException("Creating quiz failed, no ID obtained.");
            }
            
            // Insert quiz questions
            String questionSql = "INSERT INTO quiz_questions (quiz_id, question_id) VALUES (?, ?)";
            stmt = conn.prepareStatement(questionSql);
            
            for (Question question : quiz.getQuestions()) {
                stmt.setInt(1, quiz.getId());
                stmt.setInt(2, question.getId());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                throw new DatabaseException("Error rolling back transaction: " + ex.getMessage(), ex);
            }
            throw new DatabaseException("Error adding quiz: " + e.getMessage(), e);
        } finally {
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Error closing resources: " + e.getMessage(), e);
            }
        }
    }
    
    public Quiz getQuizById(int id) throws DatabaseException {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    
                    // Get questions for this quiz
                    List<Question> questions = getQuestionsForQuiz(id);
                    
                    return new Quiz(id, title, questions);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    public List<Quiz> getAllQuizzes() throws DatabaseException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quizzes";
        
        // First, collect all quiz IDs and titles
        List<Integer> quizIds = new ArrayList<>();
        List<String> quizTitles = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                quizIds.add(id);
                quizTitles.add(title);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quizzes: " + e.getMessage(), e);
        }
        
        // Now, for each quiz ID, get the questions
        for (int i = 0; i < quizIds.size(); i++) {
            int quizId = quizIds.get(i);
            String quizTitle = quizTitles.get(i);
            
            // Get questions for this quiz
            List<Question> questions = getQuestionsForQuiz(quizId);
            
            // Create the quiz object
            Quiz quiz = new Quiz(quizId, quizTitle, questions);
            quizzes.add(quiz);
        }
        
        return quizzes;
    }
    
    // Helper method to get questions for a quiz
    private List<Question> getQuestionsForQuiz(int quizId) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.* FROM questions q " +
                     "JOIN quiz_questions qq ON q.id = qq.question_id " +
                     "WHERE qq.quiz_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quizId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("id");
                    String text = rs.getString("text");
                    String optionsStr = rs.getString("options");
                    List<String> options = java.util.Arrays.asList(optionsStr.split("\\|"));
                    int correctOptionIndex = rs.getInt("correct_option_index");
                    int timeLimit = rs.getInt("time_limit");
                    
                    questions.add(new Question(questionId, text, options, correctOptionIndex, timeLimit));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving questions for quiz: " + e.getMessage(), e);
        }
        
        return questions;
    }
    
    public void updateQuiz(Quiz quiz) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Update quiz title
            String updateSql = "UPDATE quizzes SET title = ? WHERE id = ?";
            stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, quiz.getTitle());
            stmt.setInt(2, quiz.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Updating quiz failed, no rows affected.");
            }
            
            // Delete existing quiz questions
            String deleteSql = "DELETE FROM quiz_questions WHERE quiz_id = ?";
            stmt = conn.prepareStatement(deleteSql);
            stmt.setInt(1, quiz.getId());
            stmt.executeUpdate();
            
            // Insert new quiz questions
            String insertSql = "INSERT INTO quiz_questions (quiz_id, question_id) VALUES (?, ?)";
            stmt = conn.prepareStatement(insertSql);
            
            for (Question question : quiz.getQuestions()) {
                stmt.setInt(1, quiz.getId());
                stmt.setInt(2, question.getId());
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                throw new DatabaseException("Error rolling back transaction: " + ex.getMessage(), ex);
            }
            throw new DatabaseException("Error updating quiz: " + e.getMessage(), e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Error closing resources: " + e.getMessage(), e);
            }
        }
    }
    
    public void deleteQuiz(int id) throws DatabaseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Delete quiz questions
            String deleteQuestionsSql = "DELETE FROM quiz_questions WHERE quiz_id = ?";
            stmt = conn.prepareStatement(deleteQuestionsSql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            
            // Delete quiz
            String deleteQuizSql = "DELETE FROM quizzes WHERE id = ?";
            stmt = conn.prepareStatement(deleteQuizSql);
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Deleting quiz failed, no rows affected.");
            }
            
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                throw new DatabaseException("Error rolling back transaction: " + ex.getMessage(), ex);
            }
            throw new DatabaseException("Error deleting quiz: " + e.getMessage(), e);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new DatabaseException("Error closing resources: " + e.getMessage(), e);
            }
        }
    }
}
