package com.quizapp;

import com.quizapp.ui.ConsoleUI;
import com.quizapp.util.DatabaseConnection;

public class QuizApplication {
    public static void main(String[] args) {
        try {
            // Test database connection
            DatabaseConnection.getConnection();
            
            // Start the application
            ConsoleUI ui = new ConsoleUI();
            ui.start();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close database connection
            DatabaseConnection.closeConnection();
        }
    }
}
