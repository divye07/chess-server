package com.chess.network;

import com.chess.models.Game;
import com.chess.models.PieceColor;
import com.chess.models.Piece;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private Game game;
    private PieceColor playerColor;
    private PrintWriter out;
    private BufferedReader in;
    private static final ConcurrentHashMap<String, ClientHandler> activePlayers = new ConcurrentHashMap<>();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    handleCommand(inputLine);
                } catch (Exception e) {
                    System.err.println("Error processing command: " + e.getMessage());
                    out.println("ERROR:" + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (game != null) {
                String gameCode = game.getGameCode();
                ClientHandler otherPlayer = activePlayers.get(gameCode);
                if (otherPlayer != null && otherPlayer != this) {
                    otherPlayer.out.println("OPPONENT_DISCONNECTED");
                }
                activePlayers.remove(gameCode, this);
            }
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    private void handleCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            out.println("ERROR:Empty command");
            return;
        }

        String[] parts = command.split(":");
        String action = parts[0];

        try {
            switch (action) {
                case "HEARTBEAT":
                    out.println("HEARTBEAT_ACK");
                    break;

                case "CREATE_GAME":
                    handleNewGame();
                    break;

                case "JOIN_GAME":
                    if (parts.length > 1) {
                        handleJoinGame(parts[1]);
                    } else {
                        out.println("ERROR:Missing game code");
                    }
                    break;

                case "MOVE":
                    if (parts.length > 1) {
                        String[] moveParts = parts[1].split(",");
                        if (moveParts.length == 4) {
                            handleMove(moveParts);
                        } else {
                            out.println("ERROR:Invalid move format");
                        }
                    } else {
                        out.println("ERROR:Missing move coordinates");
                    }
                    break;

                case "GET_BOARD":
                    handleGetBoard();
                    break;

                default:
                    out.println("ERROR:Unknown command");
                    break;
            }
        } catch (Exception e) {
            out.println("ERROR:" + e.getMessage());
        }
    }

    private void handleNewGame() {
        game = new Game();
        playerColor = PieceColor.WHITE;
        activePlayers.put(game.getGameCode(), this);
        out.println("GAME_CREATED:" + game.getGameCode());
    }

    private void handleJoinGame(String joinCode) {
        ClientHandler otherPlayer = activePlayers.get(joinCode);
        if (otherPlayer != null && otherPlayer.game != null) {
            game = otherPlayer.game;
            playerColor = PieceColor.BLACK;
            activePlayers.put(joinCode, this);
            out.println("GAME_JOINED");
            otherPlayer.out.println("OPPONENT_JOINED");
        } else {
            out.println("ERROR:Invalid game code");
        }
    }

    private void handleMove(String[] parts) {
        if (game == null) {
            out.println("ERROR:Not in a game");
            return;
        }

        try {
            int fromRow = Integer.parseInt(parts[0]);
            int fromCol = Integer.parseInt(parts[1]);
            int toRow = Integer.parseInt(parts[2]);
            int toCol = Integer.parseInt(parts[3]);

            if (game.makeMove(fromRow, fromCol, toRow, toCol, playerColor)) {
                out.println("MOVE_ACCEPTED");
                ClientHandler otherPlayer = activePlayers.get(game.getGameCode());
                if (otherPlayer != null && otherPlayer != this) {
                    otherPlayer.out.println("OPPONENT_MOVED:" + fromRow + "," + fromCol + "," + toRow + "," + toCol);
                }
            } else {
                out.println("ERROR:Invalid move");
            }
        } catch (NumberFormatException e) {
            out.println("ERROR:Invalid move coordinates");
        }
    }

    private void handleGetBoard() {
        if (game == null) {
            out.println("ERROR:Not in a game");
            return;
        }

        StringBuilder boardState = new StringBuilder("BOARD:");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = game.getPieceAt(row, col);
                if (piece != null) {
                    boardState.append(piece.getType().name()).append(",")
                            .append(piece.getColor().name()).append(",");
                } else {
                    boardState.append("EMPTY,EMPTY,");
                }
            }
        }
        out.println(boardState.toString());
    }
} 