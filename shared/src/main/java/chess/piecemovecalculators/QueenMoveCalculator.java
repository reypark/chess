package chess.piecemovecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoveCalculator extends PieceMoveCalculator {
    private static final int[][] DIAGONAL_OFFSETS = {
            { 1, 1 },
            { 1, -1 },
            { -1, 1 },
            { -1, -1 },
    };
    private static final int[][] RANK_FILE_OFFSETS = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        this.board = board;
        Collection<ChessMove> moves = new ArrayList<>();
        moves.addAll(generateMoves(origin, RANK_FILE_OFFSETS, true));
        moves.addAll(generateMoves(origin, DIAGONAL_OFFSETS, true));
        return moves;
    }
}
