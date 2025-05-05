package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class KnightMovesCalculator implements PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        for (int[] dir : directions) {
            int row = from.getRow() + dir[0];
            int col = from.getColumn() + dir[1];

            if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition to = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(to);

                if (target == null || target.getTeamColor() != piece.getTeamColor()) {
                    moves.add(new ChessMove(from, to, null));
                }
            }
        }
        return moves;
    }
}
