package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(from);
        if (pawn == null) return moves;

        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;

        int row = from.getRow();
        int col = from.getColumn();

        ChessPosition oneForward = new ChessPosition(row + direction, col);
        if (isInBounds(oneForward) && board.getPiece(oneForward) == null) {
            moves.add(new ChessMove(from, oneForward, null));

            ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
            if (row == startRow && board.getPiece(twoForward) == null) {
                moves.add(new ChessMove(from, twoForward, null));
            }
        }

        for (int diagonalCapture = -1; diagonalCapture <= 1; diagonalCapture += 2) {
            int targetColumn = col + diagonalCapture;
            ChessPosition diagonal = new ChessPosition(row + direction, targetColumn);
            if (isInBounds(diagonal)) {
                ChessPiece target = board.getPiece(diagonal);
                if (target != null && target.getTeamColor() != pawn.getTeamColor()) {
                    moves.add(new ChessMove(from, diagonal, null));
                }
            }
        }
    return moves;
    }

    private boolean isInBounds(ChessPosition pos) {
        int r = pos.getRow(), c = pos.getColumn();
        return r >= 1 && r <= 8 && c >= 1 && c <= 8;
    }
}
