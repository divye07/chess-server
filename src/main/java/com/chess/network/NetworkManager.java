package com.chess.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class NetworkManager {
    private static String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    private static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    private static final int HEARTBEAT_INTERVAL = 5000; // 5 seconds
    private static final int RESPONSE_TIMEOUT = 10; // 10 seconds

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String gameCode;
    private boolean isConnected;
    private Thread responseThread;
    private Thread heartbeatThread;
    private final BlockingQueue<String> responseQueue;

    public NetworkManager() {
        isConnected = false;
        responseQueue = new LinkedBlockingQueue<>();
    }

    public static void setServerHost(String host) {
        SERVER_HOST = host;
    }

    public boolean connect() {
        if (isConnected) {
            return true;
        }

        try {
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(SERVER_HOST, SERVER_PORT), CONNECTION_TIMEOUT);
            socket.setSoTimeout(CONNECTION_TIMEOUT);
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            isConnected = true;
            startResponseHandler();
            startHeartbeat();
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    private void startResponseHandler() {
        responseThread = new Thread(() -> {
            try {
                String response;
                while (isConnected && (response = in.readLine()) != null) {
                    if (response.equals("HEARTBEAT_ACK")) {
                        continue;
                    }
                    responseQueue.offer(response);
                }
            } catch (IOException e) {
                if (isConnected) {
                    System.err.println("Error reading from server: " + e.getMessage());
                    disconnect();
                }
            }
        });
        responseThread.setDaemon(true);
        responseThread.start();
    }

    private void startHeartbeat() {
        heartbeatThread = new Thread(() -> {
            while (isConnected) {
                try {
                    out.println("HEARTBEAT");
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    if (isConnected) {
                        System.err.println("Heartbeat error: " + e.getMessage());
                        disconnect();
                    }
                    break;
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private String waitForResponse() {
        try {
            String response = responseQueue.poll(RESPONSE_TIMEOUT, TimeUnit.SECONDS);
            if (response == null) {
                System.err.println("No response received from server");
                disconnect();
                return null;
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public String createNewGame() {
        if (!isConnected) {
            return null;
        }

        try {
            out.println("CREATE_GAME");
            String response = waitForResponse();
            if (response != null && response.startsWith("GAME_CREATED:")) {
                gameCode = response.substring("GAME_CREATED:".length());
                return gameCode;
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error creating game: " + e.getMessage());
            disconnect();
            return null;
        }
    }

    public boolean joinGame(String code) {
        if (!isConnected) {
            return false;
        }

        try {
            out.println("JOIN_GAME:" + code);
            String response = waitForResponse();
            if (response != null && response.equals("GAME_JOINED")) {
                gameCode = code;
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error joining game: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (!isConnected) {
            return false;
        }

        try {
            out.println(String.format("MOVE:%d,%d,%d,%d", fromRow, fromCol, toRow, toCol));
            String response = waitForResponse();
            return response != null && response.equals("MOVE_ACCEPTED");
        } catch (Exception e) {
            System.err.println("Error making move: " + e.getMessage());
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        isConnected = false;
        gameCode = null;
        
        if (heartbeatThread != null) {
            heartbeatThread.interrupt();
            heartbeatThread = null;
        }
        
        if (responseThread != null) {
            responseThread.interrupt();
            responseThread = null;
        }
        
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
        
        out = null;
        in = null;
        socket = null;
    }

    public String getGameCode() {
        return gameCode;
    }

    public boolean isConnected() {
        return isConnected;
    }
} 