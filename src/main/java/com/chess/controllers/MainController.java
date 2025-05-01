package com.chess.controllers;

import com.chess.models.Piece;
import com.chess.models.PieceColor;
import com.chess.network.NetworkManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class MainController {
    @FXML
    private GridPane chessBoard;
    
    @FXML
    private TextField gameCodeField;
    
    @FXML
    private Label statusLabel;
    
    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 60;
    
    private NetworkManager networkManager;
    private Piece selectedPiece;
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean isConnected = false;
    
    @FXML
    public void initialize() {
        networkManager = new NetworkManager();
        initializeChessBoard();
    }
    
    private void initializeChessBoard() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                
                final int finalRow = row;
                final int finalCol = col;
                square.setOnMouseClicked(event -> handleSquareClick(finalRow, finalCol));
                
                chessBoard.add(square, col, row);
            }
        }
    }
    
    private void handleSquareClick(int row, int col) {
        if (!isConnected) {
            statusLabel.setText("Not connected to a game");
            return;
        }

        if (selectedPiece == null) {
            // First click - select a piece
            selectedPiece = getPieceAt(row, col);
            if (selectedPiece != null) {
                selectedRow = row;
                selectedCol = col;
                highlightSquare(row, col, Color.YELLOW);
            }
        } else {
            // Second click - move the piece
            if (networkManager.makeMove(selectedRow, selectedCol, row, col)) {
                // Move was successful
                updateBoard();
                statusLabel.setText("Move successful");
            } else {
                statusLabel.setText("Invalid move");
            }
            
            // Reset selection
            selectedPiece = null;
            selectedRow = -1;
            selectedCol = -1;
            updateBoard();
        }
    }
    
    private Piece getPieceAt(int row, int col) {
        // TODO: Implement piece retrieval from game state
        return null;
    }
    
    private void highlightSquare(int row, int col, Color color) {
        Rectangle square = (Rectangle) chessBoard.getChildren().get(row * BOARD_SIZE + col);
        square.setFill(color);
    }
    
    private void updateBoard() {
        // TODO: Implement board update from game state
    }
    
    @FXML
    private void startNewGame() {
        if (!isConnected) {
            if (networkManager.connect()) {
                isConnected = true;
                String gameCode = networkManager.createNewGame();
                if (gameCode != null) {
                    statusLabel.setText("Game created! Code: " + gameCode);
                    gameCodeField.setText(gameCode);
                } else {
                    statusLabel.setText("Failed to create game");
                    isConnected = false;
                }
            } else {
                statusLabel.setText("Failed to connect to server");
            }
        } else {
            statusLabel.setText("Already connected to a game");
        }
    }
    
    @FXML
    private void joinGame() {
        if (!isConnected) {
            if (networkManager.connect()) {
                isConnected = true;
                String gameCode = gameCodeField.getText().trim();
                if (!gameCode.isEmpty()) {
                    if (networkManager.joinGame(gameCode)) {
                        statusLabel.setText("Joined game: " + gameCode);
                    } else {
                        statusLabel.setText("Failed to join game");
                        isConnected = false;
                    }
                } else {
                    statusLabel.setText("Please enter a game code");
                    isConnected = false;
                }
            } else {
                statusLabel.setText("Failed to connect to server");
            }
        } else {
            statusLabel.setText("Already connected to a game");
        }
    }
    
    @FXML
    private void connectToGame() {
        joinGame();
    }
} 