package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class KingMovesCalculator implements PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        int[][] directions = {
                {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}
        };

        int row = from.getRow();
        int col = from.getColumn();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 1 && newRow <= 8 && newCol >= 1 && newCol <= 8) {
                ChessPosition to = new ChessPosition(newRow, newCol);
                ChessPiece target = board.getPiece(to);

                if (target == null || target.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
        return moves;
    }
}
