package com.quizapp.model;

public class Student extends User {
    public Student(int id, String username, String password) {
        super(id, username, password, "STUDENT");
    }
    
    public Student(String username, String password) {
        super(username, password, "STUDENT");
    }
}
