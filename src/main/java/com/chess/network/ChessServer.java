package com.chess.network;

import com.chess.models.Game;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChessServer {
    private static final int DEFAULT_PORT = 5000;
    private static final int MAX_THREADS = 100;
    private static final int SHUTDOWN_TIMEOUT = 30; // seconds
    
    private static final ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<>();
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        // Get port from environment variable or use default
        String portStr = System.getenv("PORT");
        int port = portStr != null ? Integer.parseInt(portStr) : DEFAULT_PORT;

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            isRunning = false;
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            System.out.println("Server shutdown complete");
        }));

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            System.out.println("Chess Server started on port " + port);
            System.out.println("Server is listening on all network interfaces");
            System.out.println("To connect from another device, use this computer's IP address and port " + port);
            System.out.println("Press Ctrl+C to stop the server");

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected from: " + clientSocket.getInetAddress().getHostAddress());
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void removeGame(String gameCode) {
        activeGames.remove(gameCode);
    }

    public static void cleanupInactiveGames() {
        // Remove games that have been inactive for too long
        // This is a placeholder for future implementation
    }
} 