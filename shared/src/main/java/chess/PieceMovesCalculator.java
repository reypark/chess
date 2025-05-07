package chess;

import java.util.ArrayList;
import java.util.List;

public abstract class PieceMovesCalculator {
    public abstract List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board);

    protected boolean isInBounds(ChessPosition pos) {
        int r = pos.getRow(), c = pos.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }

    protected boolean isOpponentPiece(ChessPiece movingPiece, ChessPiece targetPiece) {
        return targetPiece != null && targetPiece.getTeamColor() != movingPiece.getTeamColor();
    }

    protected List<ChessMove> generateSlidingMoves(ChessPosition from, ChessBoard board, ChessPiece piece, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] dir : directions) {
            int row = from.getRow();
            int col = from.getColumn();

            while (true) {
                row += dir[0];
                col += dir[1];
                ChessPosition to = new ChessPosition(row, col);

                if (!isInBounds(to)) {
                    break;
                }

                ChessPiece target = board.getPiece(to);
                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (isOpponentPiece(piece, target)) {
                        moves.add(new ChessMove(from, to, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }

    protected List<ChessMove> generateSingleStepMoves(ChessPosition from, ChessBoard board, ChessPiece piece, int[][] directions) {
        List<ChessMove> moves = new ArrayList<>();
        int row = from.getRow();
        int col = from.getColumn();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            ChessPosition to = new ChessPosition(newRow, newCol);

            if (isInBounds(to)) {
                ChessPiece target = board.getPiece(to);
                if (target == null || isOpponentPiece(piece, target)) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
        return moves;
    }

    protected List<ChessMove> generateJumpMoves(ChessPosition from, ChessBoard board, ChessPiece piece, int[][] jumps) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] jump : jumps) {
            int newRow = from.getRow() + jump[0];
            int newCol = from.getColumn() + jump[1];
            ChessPosition to = new ChessPosition(newRow, newCol);

            if (isInBounds(to)) {
                ChessPiece target = board.getPiece(to);
                if (target == null || isOpponentPiece(piece, target)) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
        return moves;
    }
}
