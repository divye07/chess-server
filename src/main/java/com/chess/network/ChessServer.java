package com.chess.network;

import com.chess.models.Game;
import org.java_websocket.server.WebSocketServer;

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

        ChessWebSocketServer wsServer = new ChessWebSocketServer(port);
        wsServer.start();
        System.out.println("WebSocket server started on port " + port);

        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Server interrupted: " + e.getMessage());
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