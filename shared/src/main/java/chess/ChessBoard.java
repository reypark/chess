package chess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        clearBoard();

        setupBackRank(ChessGame.TeamColor.WHITE, 1);
        setupPawns(ChessGame.TeamColor.WHITE, 2);
        setupPawns(ChessGame.TeamColor.BLACK, 7);
        setupBackRank(ChessGame.TeamColor.BLACK, 8);
    }

    private void clearBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = null;
            }
        }
    }

    private void setupPawns(ChessGame.TeamColor color, int row) {
        for (int col = 1; col <= 8; col++) {
            ChessPosition pos = new ChessPosition(row, col);
            ChessPiece pawn = new ChessPiece(color, ChessPiece.PieceType.PAWN);
            addPiece(pos, pawn);
        }
    }

    private void setupBackRank(ChessGame.TeamColor color, int row) {
        ChessPiece.PieceType[] order = new ChessPiece.PieceType[] {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int i = 0; i < order.length; i++) {
            ChessPosition pos = new ChessPosition(row, i + 1);
            ChessPiece piece = new ChessPiece(color, order[i]);
            addPiece(pos, piece);
        }
    }

    public List<ChessPosition> getAllPositions() {
        List<ChessPosition> positions = new ArrayList<>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                positions.add(new ChessPosition(row, col));
            }
        }
        return positions;
    }

    public ChessPosition findKingPosition(ChessGame.TeamColor color) {
        for (ChessPosition position : getAllPositions()) {
            ChessPiece piece = getPiece(position);
            if (piece != null
                    && piece.getTeamColor() == color
                    && piece.getPieceType() == ChessPiece.PieceType.KING) {
                return position;
            }
        }
        throw new IllegalStateException("No king found for " + color);
    }

    public ChessBoard deepCopy() {
        ChessBoard clone = new ChessBoard();
        for (ChessPosition position : getAllPositions()) {
            ChessPiece piece = getPiece(position);
            if (piece != null) {
                clone.addPiece(position, piece);
            }
        }
        return clone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
