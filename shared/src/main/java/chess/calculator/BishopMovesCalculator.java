package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class BishopMovesCalculator implements PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        int[][] directions = {
                {1, -1}, {1, 1}, {-1, -1}, {-1, 1}
        };

        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        for (int[] dir : directions) {
            int row = from.getRow();
            int col = from.getColumn();
            while (true) {
                row += dir[0];
                col += dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) break;

                ChessPosition to = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(to);

                if (target == null) {
                    moves.add(new ChessMove(from, to, null));
                } else {
                    if (target.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(from, to, null)); // capture
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
