package com.quizapp.service;

import com.quizapp.dao.QuestionDAO;
import com.quizapp.dao.QuizDAO;
import com.quizapp.dao.QuizResultDAO;
import com.quizapp.exception.DatabaseException;
import com.quizapp.model.Question;
import com.quizapp.model.Quiz;
import com.quizapp.model.QuizResult;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QuizService {
    private QuestionDAO questionDAO;
    private QuizDAO quizDAO;
    private QuizResultDAO quizResultDAO;
    private Scanner scanner;
    
    public QuizService() {
        this.questionDAO = new QuestionDAO();
        this.quizDAO = new QuizDAO();
        this.quizResultDAO = new QuizResultDAO();
        this.scanner = new Scanner(System.in);
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
        
        for (Question question : quiz.getQuestions()) {
            score += takeQuestion(question) ? 1 : 0;
        }
        
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
        
        AtomicBoolean timeExpired = new AtomicBoolean(false);
        AtomicBoolean answered = new AtomicBoolean(false);
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] selectedOption = new int[1]; 
        selectedOption[0] = -1;
        
        // Create a timer thread
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> {
            if (!answered.get()) {
                timeExpired.set(true);
                System.out.println("\nTime's up! Moving to the next question.");
                latch.countDown();
            }
        }, question.getTimeLimit(), TimeUnit.SECONDS);
        
        // Create a separate thread for user input
        Thread inputThread = new Thread(() -> {
            System.out.print("Enter your answer (1-" + question.getOptions().size() + "): ");
            
            try {
                while (!timeExpired.get() && !answered.get()) {
                    // Check if there's input available in the console
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine();
                        try {
                            int option = Integer.parseInt(input);
                            if (option >= 1 && option <= question.getOptions().size()) {
                                selectedOption[0] = option; // Store the selected option
                                answered.set(true);
                                System.out.println("Confirm your Answer...");
                                latch.countDown(); 
                                break;
                            } else {
                                System.out.println("Invalid option. Please try again.");
                                System.out.print("Enter your answer (1-" + question.getOptions().size() + "): ");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a number.");
                            System.out.print("Enter your answer (1-" + question.getOptions().size() + "): ");
                        }
                    }
                 
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
              
            }
        });
        
     
        inputThread.start();
        
        try {

            latch.await();
            
           
            executor.shutdownNow();
            if (!answered.get()) {
                inputThread.interrupt();
            }
            
      
            inputThread.join(1000);

            if (inputThread.isAlive()) {
                inputThread.interrupt();
            }

            while (scanner.hasNextLine() && scanner.nextLine().isEmpty()) {
                
            }
            

            if (answered.get()) {
            
                return question.isCorrect(selectedOption[0] - 1);
            }
        } catch (InterruptedException e) {
            System.err.println("Question interrupted: " + e.getMessage());
        }
        
        return false;
    }
}
