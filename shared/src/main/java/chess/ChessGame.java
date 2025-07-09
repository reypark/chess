package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        var rawMoves = piece.pieceMoves(board, startPosition);
        List<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : rawMoves) {
            ChessBoard snapshotBoard = board.deepCopy();
            snapshotBoard.addPiece(move.getEndPosition(), piece);
            snapshotBoard.addPiece(startPosition, null);
            ChessGame simulatedGame = new ChessGame();
            simulatedGame.setBoard(snapshotBoard);
            simulatedGame.setTeamTurn(teamTurn);
            if (!simulatedGame.isInCheck(teamTurn)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException("No piece at start position");
        }

        if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("It's not " + piece.getTeamColor() + "'s turn");
        }

        ChessPiece destPiece = board.getPiece(move.getEndPosition());
        if (destPiece != null && destPiece.getTeamColor() == piece.getTeamColor()) {
            throw new InvalidMoveException("Cannot capture own piece");
        }

        board.addPiece(move.getEndPosition(), piece);
        board.addPiece(move.getStartPosition(), null);

        if (teamTurn == TeamColor.WHITE) {
            teamTurn = TeamColor.BLACK;
        } else {
            teamTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = board.findKingPosition(teamColor);

        for (ChessPosition position : board.getAllPositions()) {
            ChessPiece piece = board.getPiece(position);
            if (piece != null && piece.getTeamColor() != teamColor) {
                for (ChessMove move : piece.pieceMoves(board, position)) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (ChessPosition origin : board.getAllPositions()) {
            ChessPiece piece = board.getPiece(origin);
            if (piece == null || piece.getTeamColor() != teamColor) {
                continue;
            }

            for (ChessMove move : validMoves(origin)) {
                ChessBoard testBoard = board.deepCopy();
                testBoard.addPiece(move.getEndPosition(), piece);
                testBoard.addPiece(origin, null);

                ChessGame testGame = new ChessGame();
                testGame.setBoard(testBoard);
                testGame.setTeamTurn(teamColor);

                if (!testGame.isInCheck(teamColor)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn);
    }
}
