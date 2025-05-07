package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class QueenMovesCalculator extends PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };

        return generateSlidingMoves(from, board, piece, directions);
    }
}
