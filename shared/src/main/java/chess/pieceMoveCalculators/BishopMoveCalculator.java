package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.ArrayList;
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
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece bishop = board.getPiece(origin);
        if (bishop == null) {
            return moves;
        }
        var myColor = bishop.getTeamColor();

        int startRow = origin.getRow();
        int startCol = origin.getColumn();

        for (int[] offset : DIAGONAL_OFFSETS) {
            int dRow = offset[0];
            int dColumn = offset[1];
            int row = startRow;
            int column = startCol;
            while (true) {
                row += dRow;
                column += dColumn;
                if (!isWithinBounds(row, column)) {
                    break;
                }

                ChessPosition destination = new ChessPosition(row, column);
                ChessPiece occupant = board.getPiece(destination);

                if (occupant == null) {
                    moves.add(new ChessMove(origin, destination, null));
                } else {
                    if (occupant.getTeamColor() != myColor) {
                        moves.add(new ChessMove(origin, destination, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
