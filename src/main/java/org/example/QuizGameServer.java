package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class QuizGameServer {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50051).addService(new ServiceQuizGame()).build();
        System.out.println("Server started, listening on 50051");
        server.start();
        server.awaitTermination();
    }
}

