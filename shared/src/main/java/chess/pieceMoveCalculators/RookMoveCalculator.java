package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public class RookMoveCalculator extends PieceMoveCalculator {
    private static final int[][] RANK_FILE_OFFSETS = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        this.board = board;
        return generateMoves(origin, RANK_FILE_OFFSETS, true);
    }
}
