package org.example;

import io.grpc.stub.StreamObserver;
import quizgame.QuizGameGrpc;
import quizgame.QuizOuterClass.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceQuizGame extends QuizGameGrpc.QuizGameImplBase {

    private final List<Quiz> quizzes = new ArrayList<>();
    private final Map<String, Player> players = new HashMap<>();

    public ServiceQuizGame() {
        // Initialize with sample quizzes
        quizzes.add(Quiz.newBuilder().setId(1).setQuestion("What is 2 + 2?")
                .setAnswer1("3").setAnswer2("4").setAnswer3("5").setAnswer4("6").setCorrectAnswer(2).build());
        quizzes.add(Quiz.newBuilder().setId(2).setQuestion("What is the capital of France?")
                .setAnswer1("Berlin").setAnswer2("London").setAnswer3("Paris").setAnswer4("Madrid").setCorrectAnswer(3).build());
    }

    @Override
    public void registerPlayer(RegisterPlayerRequest request, StreamObserver<RegisterPlayerResponse> responseObserver) {
        Player player = Player.newBuilder().setPlayerName(request.getPlayerName()).setScore(0).build();
        players.put(request.getPlayerName(), player);
        responseObserver.onNext(RegisterPlayerResponse.newBuilder().setPlayer(player).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getQuiz(GetQuizRequest request, StreamObserver<GetQuizResponse> responseObserver) {
        responseObserver.onNext(GetQuizResponse.newBuilder().addAllQuizzes(quizzes).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getQuestion(GetQuestionRequest request, StreamObserver<GetQuestionResponse> responseObserver) {
        quizzes.stream().filter(q -> q.getId() == request.getId()).findFirst().ifPresentOrElse(
                quiz -> responseObserver.onNext(GetQuestionResponse.newBuilder().setQuiz(quiz).build()),
                () -> responseObserver.onError(new Exception("Quiz not found"))
        );
        responseObserver.onCompleted();
    }

    @Override
    public void play(PlayRequest request, StreamObserver<PlayResponse> responseObserver) {
        Player player = players.get(request.getPlayerName());
        Quiz quiz = quizzes.stream().filter(q -> q.getId() == request.getQuizId()).findFirst().orElse(null);

        if (player != null && quiz != null) {
            boolean correct = request.getAnswer() == quiz.getCorrectAnswer();
            if (correct) player = player.toBuilder().setScore(player.getScore() + 1).build();
            players.put(player.getPlayerName(), player);
            responseObserver.onNext(PlayResponse.newBuilder().setCorrect(correct).setNewScore(player.getScore()).build());
        } else {
            responseObserver.onError(new Exception("Player or Quiz not found"));
        }
        responseObserver.onCompleted();
    }
}
