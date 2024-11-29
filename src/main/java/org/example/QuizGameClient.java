package org.example;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import quizgame.QuizGameGrpc;
import quizgame.QuizOuterClass.*;

public class QuizGameClient {

    private final QuizGameGrpc.QuizGameBlockingStub stub;

    public QuizGameClient(ManagedChannel channel) {
        this.stub = QuizGameGrpc.newBlockingStub(channel);
    }

    public void registerAndPlayQuiz(String playerName, int quizId, int answer) {
        // Register player
        RegisterPlayerResponse registerResponse = stub.registerPlayer(RegisterPlayerRequest.newBuilder().setPlayerName(playerName).build());
        System.out.println("Registered Player: " + registerResponse.getPlayer().getPlayerName() + " with Score: " + registerResponse.getPlayer().getScore());

        // Get quiz question
        GetQuestionResponse questionResponse = stub.getQuestion(GetQuestionRequest.newBuilder().setId(quizId).build());
        System.out.println("Question: " + questionResponse.getQuiz().getQuestion());

        // Answer the quiz
        PlayResponse playResponse = stub.play(PlayRequest.newBuilder().setPlayerName(playerName).setQuizId(quizId).setAnswer(answer).build());
        String result = playResponse.getCorrect() ? "Correct" : "Incorrect";
        System.out.println("Answer is " + result + ". New Score: " + playResponse.getNewScore());
    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();

        QuizGameClient client1 = new QuizGameClient(channel);
        client1.registerAndPlayQuiz("Player1", 1, 2); // Example: Player 1 answers question 1 with answer 2

        QuizGameClient client2 = new QuizGameClient(channel);
        client2.registerAndPlayQuiz("Player2", 2, 3); // Example: Player 2 answers question 2 with answer 3

        channel.shutdown();
    }
}

