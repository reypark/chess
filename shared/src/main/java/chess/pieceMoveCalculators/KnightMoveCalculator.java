package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class KnightMoveCalculator extends PieceMoveCalculator {
    private static final int[][] L_SHAPE_OFFSETS = {
            { 2, 1 },
            { 2, -1 },
            { -2, 1 },
            { -2, -1 },
            { 1, 2 },
            { 1, -2 },
            { -1, 2 },
            { -1, -2 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        this.board = board;
        return generateMoves(origin, L_SHAPE_OFFSETS, false);
    }
}
