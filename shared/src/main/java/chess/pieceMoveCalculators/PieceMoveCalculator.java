package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.Collection;

public class PieceMoveCalculator {

    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition pos) {
        return new ArrayList<>();
    }

    protected boolean isWithinBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}
