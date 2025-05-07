package chess;

import chess.calculator.*;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case BISHOP -> new BishopMovesCalculator().calculateLegalMoves(myPosition, board);
            case ROOK -> new RookMovesCalculator().calculateLegalMoves(myPosition, board);
            case KNIGHT -> new KnightMovesCalculator().calculateLegalMoves(myPosition, board);
            case QUEEN -> new QueenMovesCalculator().calculateLegalMoves(myPosition, board);
            case PAWN -> new PawnMovesCalculator().calculateLegalMoves(myPosition, board);
            case KING -> new KingMovesCalculator().calculateLegalMoves(myPosition, board);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece other)) {
            return false;
        }
        return getTeamColor() == other.getTeamColor() &&
                getPieceType() == other.getPieceType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTeamColor(), getPieceType());
    }
}