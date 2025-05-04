package com.quizapp.dao;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizDAO {
    
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
                        addQuizQuestion(conn, quiz.getId(), question.getId());
                    }
                } else {
                    throw new DatabaseException("Creating quiz failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error adding quiz: " + e.getMessage(), e);
        }
    }
    
    private void addQuizQuestion(Connection conn, int quizId, int questionId) throws DatabaseException {
        String sql = "INSERT INTO quiz_questions (quiz_id, question_id) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quizId);
            stmt.setInt(2, questionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error adding quiz question: " + e.getMessage(), e);
        }
    }
    
    public Quiz getQuizById(int id) throws DatabaseException {
        // First, get the quiz details
        Quiz quiz = null;
        String quizSql = "SELECT * FROM quizzes WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(quizSql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    quiz = new Quiz(id, title, new ArrayList<>());
                }
            }
            
            if (quiz != null) {
                // Now get the questions for this quiz
                String questionSql = 
                    "SELECT q.id, q.text, q.options, q.correct_option_index, q.time_limit " +
                    "FROM questions q " +
                    "JOIN quiz_questions qq ON q.id = qq.question_id " +
                    "WHERE qq.quiz_id = ?";
                
                try (PreparedStatement qStmt = conn.prepareStatement(questionSql)) {
                    qStmt.setInt(1, id);
                    
                    try (ResultSet qRs = qStmt.executeQuery()) {
                        List<Question> questions = new ArrayList<>();
                        while (qRs.next()) {
                            int qId = qRs.getInt("id");
                            String text = qRs.getString("text");
                            String optionsStr = qRs.getString("options");
                            List<String> options = Arrays.asList(optionsStr.split("\\|"));
                            int correctOptionIndex = qRs.getInt("correct_option_index");
                            int timeLimit = qRs.getInt("time_limit");
                            
                            questions.add(new Question(qId, text, options, correctOptionIndex, timeLimit));
                        }
                        quiz.setQuestions(questions);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quiz: " + e.getMessage(), e);
        }
        
        return quiz;
    }
    
    public List<Quiz> getAllQuizzes() throws DatabaseException {
        Map<Integer, Quiz> quizMap = new HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // First, get all quizzes
            String quizSql = "SELECT * FROM quizzes";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(quizSql)) {
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    quizMap.put(id, new Quiz(id, title, new ArrayList<>()));
                }
            }
            
            // If we have quizzes, get all questions for all quizzes in one query
            if (!quizMap.isEmpty()) {
                String questionSql = 
                    "SELECT qq.quiz_id, q.id, q.text, q.options, q.correct_option_index, q.time_limit " +
                    "FROM questions q " +
                    "JOIN quiz_questions qq ON q.id = qq.question_id";
                
                try (Statement qStmt = conn.createStatement();
                     ResultSet qRs = qStmt.executeQuery(questionSql)) {
                    
                    while (qRs.next()) {
                        int quizId = qRs.getInt("quiz_id");
                        int qId = qRs.getInt("id");
                        String text = qRs.getString("text");
                        String optionsStr = qRs.getString("options");
                        List<String> options = Arrays.asList(optionsStr.split("\\|"));
                        int correctOptionIndex = qRs.getInt("correct_option_index");
                        int timeLimit = qRs.getInt("time_limit");
                        
                        Question question = new Question(qId, text, options, correctOptionIndex, timeLimit);
                        
                        // Add this question to its quiz
                        Quiz quiz = quizMap.get(quizId);
                        if (quiz != null) {
                            quiz.getQuestions().add(question);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving quizzes: " + e.getMessage(), e);
        }
        
        return new ArrayList<>(quizMap.values());
    }
}
