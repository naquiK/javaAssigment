package com.quizapp.model;

import java.time.LocalDateTime;

public class QuizResult {
    private int id;
    private int studentId;
    private int quizId;
    private int score;
    private int totalQuestions;
    private LocalDateTime completedAt;
    
    public QuizResult(int id, int studentId, int quizId, int score, int totalQuestions, LocalDateTime completedAt) {
        this.id = id;
        this.studentId = studentId;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = completedAt;
    }
    
    public QuizResult(int studentId, int quizId, int score, int totalQuestions) {
        this.studentId = studentId;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public int getQuizId() {
        return quizId;
    }
    
    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }
    
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public double getPercentage() {
        return (double) score / totalQuestions * 100;
    }
}
