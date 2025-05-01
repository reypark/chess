package chess;

import java.util.List;

public interface PieceMovesCalculator {
    List<ChessMove> calculateLegalMoves(ChessPosition from, ChessBoard board);
}
