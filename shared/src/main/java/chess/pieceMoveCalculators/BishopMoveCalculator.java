package chess.pieceMoveCalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;

import java.util.ArrayList;
import java.util.Collection;

public class BishopMoveCalculator extends PieceMoveCalculator {
    private static final int[][] DIAGONALS = {
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

        for (int[] d : DIAGONALS) {
            int dRow = d[0];
            int dColumn = d[1];
            int row = startRow;
            int column = startCol;
            while (true) {
                row += dRow;
                column += dColumn;
                if (!isWithinBounds(row, column)) {
                    break;
                }

                ChessPosition destination = new ChessPosition(row, column);
                ChessPiece occ = board.getPiece(destination);

                if (occ == null) {
                    moves.add(new ChessMove(origin, destination, null));
                } else {
                    if (occ.getTeamColor() != myColor) {
                        moves.add(new ChessMove(origin, destination, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
