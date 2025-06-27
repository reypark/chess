package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMoveCalculator extends PieceMoveCalculator {
    private static final int[][] DIAGONAL_OFFSETS = {
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
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece knight = board.getPiece(origin);
        if (knight == null) {
            return moves;
        }
        var myColor = knight.getTeamColor();

        int startRow = origin.getRow();
        int startCol = origin.getColumn();

        for (int[] m : DIAGONAL_OFFSETS) {
            int dRow = m[0];
            int dColumn = m[1];
            int row = startRow + dRow;
            int column = startCol + dColumn;

            if (!isWithinBounds(row, column)) {
                continue;
            }

            ChessPosition destination = new ChessPosition(row, column);
            ChessPiece occupant = board.getPiece(destination);

            if (occupant == null || occupant.getTeamColor() != myColor) {
                moves.add(new ChessMove(origin, destination, null));
            }
        }
        return moves;
    }
}
