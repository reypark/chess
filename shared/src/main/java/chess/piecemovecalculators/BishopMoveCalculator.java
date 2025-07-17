package chess.piecemovecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class BishopMoveCalculator extends PieceMoveCalculator {
    private static final int[][] DIAGONAL_OFFSETS = {
            { 1, 1 },
            { 1, -1 },
            { -1, 1 },
            { -1, -1 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        this.board = board;
        return generateMoves(origin, DIAGONAL_OFFSETS, true);
    }
}
