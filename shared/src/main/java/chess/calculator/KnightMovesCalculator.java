package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class KnightMovesCalculator extends PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(from);
        if (piece == null) return moves;

        int[][] directions = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        return generateJumpMoves(from, board, piece, directions);
    }
}
