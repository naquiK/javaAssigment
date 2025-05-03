package com.quizapp.model;

import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private List<Question> questions;
    
    public Quiz(int id, String title, List<Question> questions) {
        this.id = id;
        this.title = title;
        this.questions = questions;
    }
    
    public Quiz(String title, List<Question> questions) {
        this.title = title;
        this.questions = questions;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public int getTotalQuestions() {
        return questions.size();
    }
}
