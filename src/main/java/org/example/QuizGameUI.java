package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import quizgame.QuizGameGrpc;
import quizgame.QuizOuterClass.*;

public class QuizGameUI extends JFrame {

    private QuizGameGrpc.QuizGameBlockingStub stub;
    private String playerName;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private int totalScore = 0;
    private java.util.List<Quiz> quizzes;

    // GUI Components
    private JLabel questionLabel;
    private JRadioButton[] answerButtons;
    private ButtonGroup answerGroup;
    private JButton submitButton;

    public QuizGameUI() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
        stub = QuizGameGrpc.newBlockingStub(channel);

        // Initialize GUI
        setTitle("Quiz Game");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(6, 1));

        // Registration
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
        if (playerName == null || playerName.isEmpty()) {
            System.exit(0);
        }
        stub.registerPlayer(RegisterPlayerRequest.newBuilder().setPlayerName(playerName).build());

        // Retrieve quizzes
        GetQuizResponse quizResponse = stub.getQuiz(GetQuizRequest.newBuilder().build());
        quizzes = quizResponse.getQuizzesList();

        // GUI Components
        questionLabel = new JLabel();
        add(questionLabel);

        answerButtons = new JRadioButton[4];
        answerGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            answerButtons[i] = new JRadioButton();
            answerGroup.add(answerButtons[i]);
            add(answerButtons[i]);
        }

        submitButton = new JButton("Submit Answer");
        submitButton.addActionListener(new SubmitButtonListener());
        add(submitButton);

        displayQuestion();
    }

    private void displayQuestion() {
        if (currentQuestionIndex < quizzes.size()) {
            Quiz quiz = quizzes.get(currentQuestionIndex);
            questionLabel.setText("Q" + (currentQuestionIndex + 1) + ": " + quiz.getQuestion());
            answerButtons[0].setText(quiz.getAnswer1());
            answerButtons[1].setText(quiz.getAnswer2());
            answerButtons[2].setText(quiz.getAnswer3());
            answerButtons[3].setText(quiz.getAnswer4());
            answerGroup.clearSelection();
        } else {
            showFinalScore();
        }
    }

    private void showFinalScore() {
        submitButton.setText("SHOW FINAL SCORE");
        submitButton.removeActionListener(submitButton.getActionListeners()[0]);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Display final score
                JOptionPane.showMessageDialog(QuizGameUI.this,
                        "Player: " + playerName + "\n" +
                                "Correct Answers: " + correctAnswers + "\n" +
                                "Incorrect Answers: " + incorrectAnswers + "\n" +
                                "Total Score: " + totalScore,
                        "Final Score",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        });
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Determine selected answer
            int selectedAnswer = -1;
            for (int i = 0; i < answerButtons.length; i++) {
                if (answerButtons[i].isSelected()) {
                    selectedAnswer = i + 1;
                    break;
                }
            }

            if (selectedAnswer == -1) {
                JOptionPane.showMessageDialog(QuizGameUI.this, "Please select an answer.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Quiz quiz = quizzes.get(currentQuestionIndex);
            PlayResponse playResponse = stub.play(PlayRequest.newBuilder()
                    .setPlayerName(playerName)
                    .setQuizId(quiz.getId())
                    .setAnswer(selectedAnswer)
                    .build());

            if (playResponse.getCorrect()) {
                correctAnswers++;
            } else {
                incorrectAnswers++;
            }

            totalScore = playResponse.getNewScore();
            currentQuestionIndex++;

            // Update button text if it's the last question
            if (currentQuestionIndex == quizzes.size()) {
                submitButton.setText("SHOW FINAL SCORE");
            }

            displayQuestion();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuizGameUI().setVisible(true));
    }
}

