package com.chess.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private Piece[][] board;
    private PieceColor currentTurn;
    private String gameCode;
    private boolean isGameOver;
    private List<String> moveHistory;
    private PieceColor winner;

    public Game() {
        this.gameCode = UUID.randomUUID().toString().substring(0, 8);
        this.board = new Piece[8][8];
        this.currentTurn = PieceColor.WHITE;
        this.isGameOver = false;
        this.moveHistory = new ArrayList<>();
        this.winner = null;
        initializeBoard();
    }

    private void initializeBoard() {
        // Initialize pawns
        for (int col = 0; col < 8; col++) {
            board[1][col] = new Piece(PieceType.PAWN, PieceColor.BLACK, 1, col);
            board[6][col] = new Piece(PieceType.PAWN, PieceColor.WHITE, 6, col);
        }

        // Initialize rooks
        board[0][0] = new Piece(PieceType.ROOK, PieceColor.BLACK, 0, 0);
        board[0][7] = new Piece(PieceType.ROOK, PieceColor.BLACK, 0, 7);
        board[7][0] = new Piece(PieceType.ROOK, PieceColor.WHITE, 7, 0);
        board[7][7] = new Piece(PieceType.ROOK, PieceColor.WHITE, 7, 7);

        // Initialize knights
        board[0][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK, 0, 1);
        board[0][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK, 0, 6);
        board[7][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE, 7, 1);
        board[7][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE, 7, 6);

        // Initialize bishops
        board[0][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK, 0, 2);
        board[0][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK, 0, 5);
        board[7][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE, 7, 2);
        board[7][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE, 7, 5);

        // Initialize queens
        board[0][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK, 0, 3);
        board[7][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE, 7, 3);

        // Initialize kings
        board[0][4] = new Piece(PieceType.KING, PieceColor.BLACK, 0, 4);
        board[7][4] = new Piece(PieceType.KING, PieceColor.WHITE, 7, 4);
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        if (isGameOver) {
            return false;
        }

        if (playerColor != currentTurn) {
            return false;
        }

        if (!isValidPosition(fromRow, fromCol) || !isValidPosition(toRow, toCol)) {
            return false;
        }

        Piece piece = board[fromRow][fromCol];
        if (piece == null || piece.getColor() != playerColor) {
            return false;
        }

        if (!piece.isValidMove(toRow, toCol, board)) {
            return false;
        }

        // Check if the move would put the king in check
        if (wouldPutKingInCheck(fromRow, fromCol, toRow, toCol, playerColor)) {
            return false;
        }

        // Store the move in history
        moveHistory.add(String.format("%d%d%d%d", fromRow, fromCol, toRow, toCol));

        // Make the move
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        piece.setPosition(toRow, toCol);

        // Switch turns
        currentTurn = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        // Check for game over conditions
        checkGameOver();

        return true;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    private boolean wouldPutKingInCheck(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        // Simulate the move
        Piece originalPiece = board[toRow][toCol];
        Piece movingPiece = board[fromRow][fromCol];
        
        board[toRow][toCol] = movingPiece;
        board[fromRow][fromCol] = null;
        
        boolean isInCheck = isKingInCheck(playerColor);
        
        // Undo the move
        board[fromRow][fromCol] = movingPiece;
        board[toRow][toCol] = originalPiece;
        
        return isInCheck;
    }

    private boolean isKingInCheck(PieceColor color) {
        int kingRow = -1;
        int kingCol = -1;
        
        // Find the king's position
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
            if (kingRow != -1) break;
        }
        
        if (kingRow == -1) return false;
        
        // Check if any opponent's piece can attack the king
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board[row][col];
                if (piece != null && piece.getColor() != color) {
                    if (piece.isValidMove(kingRow, kingCol, board)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    private void checkGameOver() {
        // Check for checkmate
        if (isKingInCheck(currentTurn)) {
            boolean hasLegalMoves = false;
            
            // Check if the current player has any legal moves
            for (int fromRow = 0; fromRow < 8; fromRow++) {
                for (int fromCol = 0; fromCol < 8; fromCol++) {
                    Piece piece = board[fromRow][fromCol];
                    if (piece != null && piece.getColor() == currentTurn) {
                        for (int toRow = 0; toRow < 8; toRow++) {
                            for (int toCol = 0; toCol < 8; toCol++) {
                                if (piece.isValidMove(toRow, toCol, board) && 
                                    !wouldPutKingInCheck(fromRow, fromCol, toRow, toCol, currentTurn)) {
                                    hasLegalMoves = true;
                                    break;
                                }
                            }
                            if (hasLegalMoves) break;
                        }
                        if (hasLegalMoves) break;
                    }
                }
                if (hasLegalMoves) break;
            }
            
            if (!hasLegalMoves) {
                isGameOver = true;
                winner = (currentTurn == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
            }
        }
        
        // Check for stalemate
        if (!isGameOver) {
            boolean hasLegalMoves = false;
            
            for (int fromRow = 0; fromRow < 8; fromRow++) {
                for (int fromCol = 0; fromCol < 8; fromCol++) {
                    Piece piece = board[fromRow][fromCol];
                    if (piece != null && piece.getColor() == currentTurn) {
                        for (int toRow = 0; toRow < 8; toRow++) {
                            for (int toCol = 0; toCol < 8; toCol++) {
                                if (piece.isValidMove(toRow, toCol, board) && 
                                    !wouldPutKingInCheck(fromRow, fromCol, toRow, toCol, currentTurn)) {
                                    hasLegalMoves = true;
                                    break;
                                }
                            }
                            if (hasLegalMoves) break;
                        }
                        if (hasLegalMoves) break;
                    }
                }
                if (hasLegalMoves) break;
            }
            
            if (!hasLegalMoves) {
                isGameOver = true;
            }
        }
    }

    public Piece[][] getBoard() {
        return board;
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    public String getGameCode() {
        return gameCode;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public PieceColor getWinner() {
        return winner;
    }

    public List<String> getMoveHistory() {
        return moveHistory;
    }

    public Piece getPieceAt(int row, int col) {
        if (isValidPosition(row, col)) {
            return board[row][col];
        }
        return null;
    }
} 