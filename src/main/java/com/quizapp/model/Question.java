package com.quizapp.model;

import java.util.ArrayList;
import java.util.List;

public class Question {
    private int id;
    private String text;
    private List<String> options;
    private int correctOptionIndex;
    private int timeLimit; // in seconds
    
    public Question(int id, String text, List<String> options, int correctOptionIndex, int timeLimit) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.timeLimit = timeLimit;
    }
    
    public Question(String text, List<String> options, int correctOptionIndex, int timeLimit) {
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.timeLimit = timeLimit;
    }
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public void setOptions(List<String> options) {
        this.options = options;
    }
    
    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }
    
    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }
    
    public int getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public boolean isCorrect(int selectedOption) {
        return selectedOption == correctOptionIndex;
    }
    
    public void display() {
        System.out.println(text);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.println("Time limit: " + timeLimit + " seconds");
    }
}
