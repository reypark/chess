package chess.calculator;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class PawnMovesCalculator extends PieceMovesCalculator {

    @Override
    public List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(from);
        if (pawn == null) return moves;

        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 8 : 1;

        int row = from.getRow();
        int col = from.getColumn();

        ChessPosition oneForward = new ChessPosition(row + direction, col);
        if (isInBounds(oneForward) && board.getPiece(oneForward) == null) {
            addMoveWithOptionalPromotion(moves, from, oneForward, promotionRow);
            if (row == startRow) {
                ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
                if (isInBounds(twoForward) && board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(from, twoForward, null));
                }
            }
        }

        for (int diagonalCapture = -1; diagonalCapture <= 1; diagonalCapture += 2) {
            int targetColumn = col + diagonalCapture;
            ChessPosition diagonal = new ChessPosition(row + direction, targetColumn);
            if (isInBounds(diagonal)) {
                ChessPiece target = board.getPiece(diagonal);
                if (target != null && target.getTeamColor() != pawn.getTeamColor()) {
                    addMoveWithOptionalPromotion(moves, from, diagonal, promotionRow);
                }
            }
        }
    return moves;
    }

    private void addMoveWithOptionalPromotion(List<ChessMove> moves, ChessPosition from, ChessPosition to, int promotionRow) {
        if (to.getRow() == promotionRow) {
            for (ChessPiece.PieceType type : new ChessPiece.PieceType[]{
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT,
                    ChessPiece.PieceType.ROOK
            }) {
                moves.add(new ChessMove(from, to, type));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
