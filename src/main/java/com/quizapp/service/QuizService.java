package com.quizapp.service;

import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.QuizDAO;
import com.quizapp.dao.QuizResultDAO;
import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizResult;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuizService {
    private QuestionDAO questionDAO;
    private QuizDAO quizDAO;
    private QuizResultDAO quizResultDAO;
    
    public QuizService() {
        this.questionDAO = new QuestionDAO();
        this.quizDAO = new QuizDAO();
        this.quizResultDAO = new QuizResultDAO();
    }
    
    public void addQuestion(Question question) throws DatabaseException {
        questionDAO.addQuestion(question);
    }
    
    public void updateQuestion(Question question) throws DatabaseException {
        questionDAO.updateQuestion(question);
    }
    
    public List<Question> getAllQuestions() throws DatabaseException {
        return questionDAO.getAllQuestions();
    }
    
    public Question getQuestionById(int id) throws DatabaseException {
        return questionDAO.getQuestionById(id);
    }
    
    public void addQuiz(Quiz quiz) throws DatabaseException {
        quizDAO.addQuiz(quiz);
    }
    
    public List<Quiz> getAllQuizzes() throws DatabaseException {
        return quizDAO.getAllQuizzes();
    }
    
    public Quiz getQuizById(int id) throws DatabaseException {
        return quizDAO.getQuizById(id);
    }
    
    public void saveQuizResult(QuizResult result) throws DatabaseException {
        quizResultDAO.addQuizResult(result);
    }
    
    public List<QuizResult> getStudentResults(int studentId) throws DatabaseException {
        return quizResultDAO.getResultsByStudentId(studentId);
    }
    
    public List<QuizResult> getAllResults() throws DatabaseException {
        return quizResultDAO.getAllResults();
    }
    
    public int takeQuiz(Quiz quiz, int studentId) {
        int score = 0;
        
        // Loop through all questions in the quiz
        for (Question question : quiz.getQuestions()) {
            score += takeQuestion(question) ? 1 : 0;
        }
        
        // Save quiz result
        try {
            QuizResult result = new QuizResult(studentId, quiz.getId(), score, quiz.getTotalQuestions());
            saveQuizResult(result);
            return score;
        } catch (DatabaseException e) {
            System.err.println("Error saving quiz result: " + e.getMessage());
            return score;
        }
    }
    
    private boolean takeQuestion(Question question) {
        question.display();
        
        // Time tracking logic
        AtomicBoolean timeExpired = new AtomicBoolean(false);
        AtomicBoolean answered = new AtomicBoolean(false);
        
        // Create a timer thread to handle time expiry
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> {
            if (!answered.get()) {
                timeExpired.set(true);
                System.out.println("\nTime's up! Moving to the next question.");
            }
        }, question.getTimeLimit(), TimeUnit.SECONDS);
        
        // Get user input
        int selectedOption = -1;
        
        while (!timeExpired.get() && !answered.get()) {
            System.out.print("Enter your answer (1-" + question.getOptions().size() + "): ");
            try {
                // Check if there's input available
                if (System.in.available() > 0) {
                    String input = new java.util.Scanner(System.in).nextLine();
                    try {
                        selectedOption = Integer.parseInt(input);
                        if (selectedOption >= 1 && selectedOption <= question.getOptions().size()) {
                            answered.set(true);
                        } else {
                            System.out.println("Invalid option. Please try again.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a number.");
                    }
                }
                // Sleep a bit to prevent CPU hogging
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }
        
        executor.shutdownNow();
        
        // Return false if time expired
        if (timeExpired.get()) {
            return false;
        }
        
        // Convert from 1-based to 0-based indexing
        return question.isCorrect(selectedOption - 1);
    }
}

