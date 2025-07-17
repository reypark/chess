package chess.piecemovecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.Collection;

public class KingMoveCalculator extends PieceMoveCalculator {
    private static final int[][] ALL_DIRECTION_OFFSETS = {
            { 1, 0 },
            { -1, 0 },
            { 0, 1 },
            { 0, -1 },
            { 1, 1 },
            { 1, -1 },
            { -1, 1 },
            { -1, -1 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece king = board.getPiece(origin);
        if (king == null) {
            return moves;
        }
        var myColor = king.getTeamColor();

        int startRow = origin.getRow();
        int startCol = origin.getColumn();

        for (int[] offset : ALL_DIRECTION_OFFSETS) {
            int dRow = offset[0];
            int dColumn = offset[1];
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
