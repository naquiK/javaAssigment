package com.quizapp.ui;

import com.quizapp.exception.DatabaseException;
import com.quizapp.model.*;
import com.quizapp.service.AuthService;
import com.quizapp.service.QuizService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private Scanner scanner;
    private AuthService authService;
    private QuizService quizService;
    
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.authService = new AuthService();
        this.quizService = new QuizService();
    }
    
    public void start() {
        boolean exit = false;
        
        while (!exit) {
            if (!authService.isLoggedIn()) {
                showLoginMenu();
            } else if (authService.isAdmin()) {
                showAdminMenu();
            } else if (authService.isStudent()) {
                showStudentMenu();
            }
            
            System.out.print("Do you want to exit? (y/n): ");
            String choice = scanner.nextLine();
            exit = choice.equalsIgnoreCase("y");
        }
        
        System.out.println("Thank you for using the Quiz Application!");
    }
    
    private void showLoginMenu() {
        System.out.println("\n===== Login =====");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        if (authService.login(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }
    
    private void showAdminMenu() {
        System.out.println("\n===== Admin Menu =====");
        System.out.println("1. Add Question");
        System.out.println("2. Update Question");
        System.out.println("3. View All Questions");
        System.out.println("4. Create Quiz");
        System.out.println("5. View All Quizzes");
        System.out.println("6. View All Student Scores");
        System.out.println("7. Logout");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1:
                    addQuestion();
                    break;
                case 2:
                    updateQuestion();
                    break;
                case 3:
                    viewAllQuestions();
                    break;
                case 4:
                    createQuiz();
                    break;
                case 5:
                    viewAllQuizzes();
                    break;
                case 6:
                    viewAllStudentScores();
                    break;
                case 7:
                    authService.logout();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
    
    private void showStudentMenu() {
        System.out.println("\n===== Student Menu =====");
        System.out.println("1. Take Quiz");
        System.out.println("2. View My Scores");
        System.out.println("3. Logout");
        System.out.print("Enter your choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            
            switch (choice) {
                case 1:
                    takeQuiz();
                    break;
                case 2:
                    viewMyScores();
                    break;
                case 3:
                    authService.logout();
                    System.out.println("Logged out successfully.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
    }
    
    private void addQuestion() {
        try {
            System.out.println("\n===== Add Question =====");
            System.out.print("Enter question text: ");
            String text = scanner.nextLine();
            
            System.out.print("Enter number of options: ");
            int numOptions = Integer.parseInt(scanner.nextLine());
            
            List<String> options = new ArrayList<>();
            for (int i = 0; i < numOptions; i++) {
                System.out.print("Enter option " + (i + 1) + ": ");
                options.add(scanner.nextLine());
            }
            
            System.out.print("Enter the index of the correct option (1-" + numOptions + "): ");
            int correctOptionIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            System.out.print("Enter time limit in seconds: ");
            int timeLimit = Integer.parseInt(scanner.nextLine());
            
            Question question = new Question(text, options, correctOptionIndex, timeLimit);
            quizService.addQuestion(question);
            
            System.out.println("Question added successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (DatabaseException e) {
            System.err.println("Error adding question: " + e.getMessage());
        }
    }
    
    private void updateQuestion() {
        try {
            viewAllQuestions();
            
            System.out.print("Enter the ID of the question to update: ");
            int id = Integer.parseInt(scanner.nextLine());
            
            Question question = quizService.getQuestionById(id);
            if (question == null) {
                System.out.println("Question not found.");
                return;
            }
            
            System.out.print("Enter new question text (or press Enter to keep current): ");
            String text = scanner.nextLine();
            if (!text.isEmpty()) {
                question.setText(text);
            }
            
            System.out.print("Do you want to update options? (y/n): ");
            String updateOptions = scanner.nextLine();
            
            if (updateOptions.equalsIgnoreCase("y")) {
                System.out.print("Enter number of options: ");
                int numOptions = Integer.parseInt(scanner.nextLine());
                
                List<String> options = new ArrayList<>();
                for (int i = 0; i < numOptions; i++) {
                    System.out.print("Enter option " + (i + 1) + ": ");
                    options.add(scanner.nextLine());
                }
                question.setOptions(options);
                
                System.out.print("Enter the index of the correct option (1-" + numOptions + "): ");
                int correctOptionIndex = Integer.parseInt(scanner.nextLine()) - 1;
                question.setCorrectOptionIndex(correctOptionIndex);
            }
            
            System.out.print("Enter new time limit in seconds (or press Enter to keep current): ");
            String timeLimitStr = scanner.nextLine();
            if (!timeLimitStr.isEmpty()) {
                int timeLimit = Integer.parseInt(timeLimitStr);
                question.setTimeLimit(timeLimit);
            }
            
            quizService.updateQuestion(question);
            System.out.println("Question updated successfully!");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (DatabaseException e) {
            System.err.println("Error updating question: " + e.getMessage());
        }
    }
    
    private void viewAllQuestions() {
        try {
            List<Question> questions = quizService.getAllQuestions();
            
            System.out.println("\n===== All Questions =====");
            if (questions.isEmpty()) {
                System.out.println("No questions found.");
                return;
            }
            
            for (Question question : questions) {
                System.out.println("ID: " + question.getId());
                System.out.println("Text: " + question.getText());
                System.out.println("Options:");
                List<String> options = question.getOptions();
                for (int i = 0; i < options.size(); i++) {
                    System.out.println((i + 1) + ". " + options.get(i) + (i == question.getCorrectOptionIndex() ? " (Correct)" : ""));
                }
                System.out.println("Time Limit: " + question.getTimeLimit() + " seconds");
                System.out.println("------------------------");
            }
        } catch (DatabaseException e) {
            System.err.println("Error retrieving questions: " + e.getMessage());
        }
    }
    
    private void createQuiz() {
        try {
            List<Question> allQuestions = quizService.getAllQuestions();
            
            if (allQuestions.isEmpty()) {
                System.out.println("No questions available. Please add questions first.");
                return;
            }
            
            System.out.println("\n===== Create Quiz =====");
            System.out.print("Enter quiz title: ");
            String title = scanner.nextLine();
            
            System.out.println("\nAvailable Questions:");
            for (Question question : allQuestions) {
                System.out.println("ID: " + question.getId() + " - " + question.getText());
            }
            
            System.out.print("Enter question IDs (comma-separated): ");
            String[] questionIds = scanner.nextLine().split(",");
            
            List<Question> quizQuestions = new ArrayList<>();
            for (String idStr : questionIds) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Question question = quizService.getQuestionById(id);
                    if (question != null) {
                        quizQuestions.add(question);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID: " + idStr + ". Skipping.");
                }
            }
            
            if (quizQuestions.isEmpty()) {
                System.out.println("No valid questions selected. Quiz creation cancelled.");
                return;
            }
            
            Quiz quiz = new Quiz(title, quizQuestions);
            quizService.addQuiz(quiz);
            
            System.out.println("Quiz created successfully!");
        } catch (DatabaseException e) {
            System.err.println("Error creating quiz: " + e.getMessage());
        }
    }
    
    private void viewAllQuizzes() {
        try {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            
            System.out.println("\n===== All Quizzes =====");
            if (quizzes.isEmpty()) {
                System.out.println("No quizzes found.");
                return;
            }
            
            for (Quiz quiz : quizzes) {
                System.out.println("ID: " + quiz.getId());
                System.out.println("Title: " + quiz.getTitle());
                System.out.println("Number of Questions: " + quiz.getTotalQuestions());
                System.out.println("------------------------");
            }
        } catch (DatabaseException e) {
            System.err.println("Error retrieving quizzes: " + e.getMessage());
        }
    }
    
    private void viewAllStudentScores() {
        try {
            List<QuizResult> results = quizService.getAllResults();
            
            System.out.println("\n===== All Student Scores =====");
            if (results.isEmpty()) {
                System.out.println("No quiz results found.");
                return;
            }
            
            for (QuizResult result : results) {
                System.out.println("Result ID: " + result.getId());
                System.out.println("Student ID: " + result.getStudentId());
                System.out.println("Quiz ID: " + result.getQuizId());
                System.out.println("Score: " + result.getScore() + "/" + result.getTotalQuestions() + 
                                  " (" + String.format("%.2f", result.getPercentage()) + "%)");
                System.out.println("Completed At: " + result.getCompletedAt());
                System.out.println("------------------------");
            }
        } catch (DatabaseException e) {
            System.err.println("Error retrieving quiz results: " + e.getMessage());
        }
    }
    
    private void takeQuiz() {
        try {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            
            if (quizzes.isEmpty()) {
                System.out.println("No quizzes available.");
                return;
            }
            
            System.out.println("\n===== Available Quizzes =====");
            for (Quiz quiz : quizzes) {
                System.out.println("ID: " + quiz.getId() + " - " + quiz.getTitle() + 
                                  " (" + quiz.getTotalQuestions() + " questions)");
            }
            
            System.out.print("Enter the ID of the quiz you want to take: ");
            int quizId = Integer.parseInt(scanner.nextLine());
            
            Quiz quiz = quizService.getQuizById(quizId);
            if (quiz == null) {
                System.out.println("Quiz not found.");
                return;
            }
            
            System.out.println("\nStarting Quiz: " + quiz.getTitle());
            System.out.println("Total Questions: " + quiz.getTotalQuestions());
            System.out.println("Quiz will begin in 3 seconds...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Ignore
            }
            
            int score = quizService.takeQuiz(quiz, authService.getCurrentUser().getId());
            
            System.out.println("\n===== Quiz Completed =====");
            System.out.println("Your Score: " + score + "/" + quiz.getTotalQuestions());
            System.out.println("Percentage: " + String.format("%.2f", (double) score / quiz.getTotalQuestions() * 100) + "%");
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        } catch (DatabaseException e) {
            System.err.println("Error taking quiz: " + e.getMessage());
        }
    }
    
    private void viewMyScores() {
        try {
            List<QuizResult> results = quizService.getStudentResults(authService.getCurrentUser().getId());
            
            System.out.println("\n===== My Quiz Scores =====");
            if (results.isEmpty()) {
                System.out.println("You haven't taken any quizzes yet.");
                return;
            }
            
            for (QuizResult result : results) {
                System.out.println("Quiz ID: " + result.getQuizId());
                System.out.println("Score: " + result.getScore() + "/" + result.getTotalQuestions() + 
                                  " (" + String.format("%.2f", result.getPercentage()) + "%)");
                System.out.println("Completed At: " + result.getCompletedAt());
                System.out.println("------------------------");
            }
        } catch (DatabaseException e) {
            System.err.println("Error retrieving your quiz results: " + e.getMessage());
        }
    }
}
