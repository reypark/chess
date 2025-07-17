package chess.piecemovecalculators;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class PieceMoveCalculator {
    protected ChessBoard board;

    public abstract Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin);

    protected boolean isWithinBounds(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    protected int getMaxSteps(boolean sliding) {
        if (sliding) {
            return Integer.MAX_VALUE;
        } else {
            return 1;
        }
    }

    protected Collection<ChessMove> generateMoves(ChessPosition origin,
                                                  int[][] offsets,
                                                  boolean sliding) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(origin);
        if (piece == null) {
            return moves;
        }
        ChessGame.TeamColor myColor = piece.getTeamColor();
        int startRow = origin.getRow();
        int startCol = origin.getColumn();

        for (int[] offset : offsets) {
            int row = startRow;
            int col = startCol;
            int maxSteps = getMaxSteps(sliding);

            for (int step = 0; step < maxSteps; step++) {
                row += offset[0];
                col += offset[1];
                if (!isWithinBounds(row, col)) {
                    break;
                }

                ChessPosition destination = new ChessPosition(row, col);
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
