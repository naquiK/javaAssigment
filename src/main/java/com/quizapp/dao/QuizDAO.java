package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {
    
    private QuestionDAO questionDAO = new QuestionDAO();
    
    public void addQuiz(Quiz quiz) throws DatabaseException {
        String sql = "INSERT INTO quizzes (title) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, quiz.getTitle());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Creating quiz failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    quiz.setId(generatedKeys.getInt(1));
                    
                    // Add quiz questions
                    for (Question question : quiz.getQuestions()) {
                        addQuizQuestion(quiz.getId(), question.getId());
                    }
                } else {
                    throw new DatabaseException("Creating quiz failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding quiz: " + e.getMessage(), e);
        }
    }
    
    private void addQuizQuestion(int quizId, int questionId) throws DatabaseException {
        String sql = "INSERT INTO quiz_questions (quiz_id, question_id) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quizId);
            stmt.setInt(2, questionId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error adding quiz question: " + e.getMessage(), e);
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
                    List<Question> questions = getQuizQuestions(id);
                    
                    return new Quiz(id, title, questions);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz: " + e.getMessage(), e);
        }
        
        return null;
    }
    
    private List<Question> getQuizQuestions(int quizId) throws DatabaseException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT q.* FROM questions q " +
                     "JOIN quiz_questions qq ON q.id = qq.question_id " +
                     "WHERE qq.quiz_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quizId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Question question = questionDAO.getQuestionById(id);
                    if (question != null) {
                        questions.add(question);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz questions: " + e.getMessage(), e);
        }
        
        return questions;
    }
    
    public List<Quiz> getAllQuizzes() throws DatabaseException {
        List<Quiz> quizzes = new ArrayList<>();
        String sql = "SELECT * FROM quizzes";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                List<Question> questions = getQuizQuestions(id);
                
                quizzes.add(new Quiz(id, title, questions));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quizzes: " + e.getMessage(), e);
        }
        
        return quizzes;
    }
}
