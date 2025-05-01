package com.chess.models;

public class Piece {
    private PieceType type;
    private PieceColor color;
    private int row;
    private int col;
    private boolean hasMoved;

    public Piece(PieceType type, PieceColor color, int row, int col) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.col = col;
        this.hasMoved = false;
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        this.hasMoved = true;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public boolean isValidMove(int newRow, int newCol, Piece[][] board) {
        // Basic bounds checking
        if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
            return false;
        }

        // Can't capture your own piece
        Piece targetPiece = board[newRow][newCol];
        if (targetPiece != null && targetPiece.getColor() == this.color) {
            return false;
        }

        // Implement specific movement rules for each piece type
        switch (type) {
            case PAWN:
                return isValidPawnMove(newRow, newCol, board);
            case ROOK:
                return isValidRookMove(newRow, newCol, board);
            case KNIGHT:
                return isValidKnightMove(newRow, newCol);
            case BISHOP:
                return isValidBishopMove(newRow, newCol, board);
            case QUEEN:
                return isValidQueenMove(newRow, newCol, board);
            case KING:
                return isValidKingMove(newRow, newCol, board);
            default:
                return false;
        }
    }

    private boolean isValidPawnMove(int newRow, int newCol, Piece[][] board) {
        int direction = (color == PieceColor.WHITE) ? -1 : 1;
        int startRow = (color == PieceColor.WHITE) ? 6 : 1;
        
        // Forward move
        if (newCol == col && board[newRow][newCol] == null) {
            // Single step forward
            if (newRow == row + direction) {
                return true;
            }
            // Double step from starting position
            if (row == startRow && newRow == row + 2 * direction && board[row + direction][col] == null) {
                return true;
            }
        }
        
        // Capture move
        if (Math.abs(newCol - col) == 1 && newRow == row + direction) {
            Piece target = board[newRow][newCol];
            return target != null && target.getColor() != color;
        }
        
        // En passant
        if (Math.abs(newCol - col) == 1 && newRow == row + direction && board[newRow][newCol] == null) {
            Piece adjacentPiece = board[row][newCol];
            if (adjacentPiece != null && adjacentPiece.getType() == PieceType.PAWN && 
                adjacentPiece.getColor() != color && adjacentPiece.hasMoved()) {
                return true;
            }
        }
        
        return false;
    }

    private boolean isValidRookMove(int newRow, int newCol, Piece[][] board) {
        // Must move in a straight line
        if (row != newRow && col != newCol) {
            return false;
        }

        // Check for pieces in the path
        int rowStep = (newRow > row) ? 1 : (newRow < row) ? -1 : 0;
        int colStep = (newCol > col) ? 1 : (newCol < col) ? -1 : 0;
        
        int currentRow = row + rowStep;
        int currentCol = col + colStep;
        
        while (currentRow != newRow || currentCol != newCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }

    private boolean isValidKnightMove(int newRow, int newCol) {
        int rowDiff = Math.abs(newRow - row);
        int colDiff = Math.abs(newCol - col);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    private boolean isValidBishopMove(int newRow, int newCol, Piece[][] board) {
        // Must move diagonally
        if (Math.abs(newRow - row) != Math.abs(newCol - col)) {
            return false;
        }

        // Check for pieces in the path
        int rowStep = (newRow > row) ? 1 : -1;
        int colStep = (newCol > col) ? 1 : -1;
        
        int currentRow = row + rowStep;
        int currentCol = col + colStep;
        
        while (currentRow != newRow && currentCol != newCol) {
            if (board[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }
        
        return true;
    }

    private boolean isValidQueenMove(int newRow, int newCol, Piece[][] board) {
        // Queen can move like a rook or bishop
        return isValidRookMove(newRow, newCol, board) || isValidBishopMove(newRow, newCol, board);
    }

    private boolean isValidKingMove(int newRow, int newCol, Piece[][] board) {
        // Regular king move
        int rowDiff = Math.abs(newRow - row);
        int colDiff = Math.abs(newCol - col);
        if (rowDiff <= 1 && colDiff <= 1) {
            return true;
        }

        // Castling
        if (!hasMoved && rowDiff == 0 && Math.abs(newCol - col) == 2) {
            int rookCol = (newCol > col) ? 7 : 0;
            Piece rook = board[row][rookCol];
            
            if (rook != null && rook.getType() == PieceType.ROOK && !rook.hasMoved()) {
                // Check if the path is clear
                int step = (newCol > col) ? 1 : -1;
                int currentCol = col + step;
                while (currentCol != rookCol) {
                    if (board[row][currentCol] != null) {
                        return false;
                    }
                    currentCol += step;
                }
                return true;
            }
        }

        return false;
    }
} 