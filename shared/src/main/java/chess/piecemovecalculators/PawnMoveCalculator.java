package chess.piecemovecalculators;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;
import chess.ChessGame;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoveCalculator extends PieceMoveCalculator {
    private static final int[][] CAPTURE_OFFSETS = {
            { 1, 1 },
            { 1, -1 },
    };

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition origin) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(origin);
        if (pawn == null) {
            return moves;
        }
        ChessGame.TeamColor color = pawn.getTeamColor();

        int direction;
        if (color == ChessGame.TeamColor.WHITE) {
            direction = 1;
        } else {
            direction = -1;
        }

        addForwardMoves(board, origin, color, direction, moves);
        addCaptureMoves(board, origin, color, direction, moves);

        return moves;
    }

    private void addForwardMoves(ChessBoard board,
                                 ChessPosition origin,
                                 ChessGame.TeamColor color,
                                 int direction,
                                 Collection<ChessMove> moves) {
        int row = origin.getRow();
        int col = origin.getColumn();
        int oneStepRow = row + direction;

        if (!isWithinBounds(oneStepRow, col)) {
            return;
        }

        ChessPosition oneStepPos = new ChessPosition(oneStepRow, col);
        if (board.getPiece(oneStepPos) != null) {
            return;
        }

        if (oneStepRow == 8 || oneStepRow == 1) {
            for (ChessPiece.PieceType type : new ChessPiece.PieceType[]{
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT
            }) {
                moves.add(new ChessMove(origin, oneStepPos, type));
            }
        } else {
            moves.add(new ChessMove(origin, oneStepPos, null));

            boolean onStartRank = (color == ChessGame.TeamColor.WHITE && row == 2)
                    || (color == ChessGame.TeamColor.BLACK && row == 7);
            if (onStartRank) {
                int twoStepRow = row + 2 * direction;
                if (isWithinBounds(twoStepRow, col)) {
                    ChessPosition twoStepPos = new ChessPosition(twoStepRow, col);
                    if (board.getPiece(twoStepPos) == null) {
                        moves.add(new ChessMove(origin, twoStepPos, null));
                    }
                }
            }
        }
    }

    private void addCaptureMoves(ChessBoard board,
                                 ChessPosition origin,
                                 ChessGame.TeamColor color,
                                 int direction,
                                 Collection<ChessMove> moves) {
        int row = origin.getRow();
        int col = origin.getColumn();

        for (int[] offset : CAPTURE_OFFSETS) {
            int captureRow = row + direction;
            int captureCol = col + offset[1];
            if (!isWithinBounds(captureRow, captureCol)) {
                continue;
            }

            ChessPosition capturePos = new ChessPosition(captureRow, captureCol);
            ChessPiece occupant = board.getPiece(capturePos);
            if (occupant == null || occupant.getTeamColor() == color) {
                continue;
            }

            if (captureRow == 8 || captureRow == 1) {
                for (ChessPiece.PieceType type : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT
                }) {
                    moves.add(new ChessMove(origin, capturePos, type));
                }
            } else {
                moves.add(new ChessMove(origin, capturePos, null));
            }
        }
    }
}