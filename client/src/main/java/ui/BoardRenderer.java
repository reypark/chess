package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

public class BoardRenderer {
    public static void draw(ChessGame game, ChessGame.TeamColor perspective) {
        System.out.print(EscapeSequences.ERASE_SCREEN);

        ChessBoard board = game.getBoard();

        int rowStart, rowEnd, rowStep;
        char fileStart, fileEnd;
        int fileStep;
        if (perspective == ChessGame.TeamColor.WHITE) {
            rowStart = 8; rowEnd = 1; rowStep = -1;
            fileStart = 'a'; fileEnd = 'h'; fileStep = +1;
        } else {
            rowStart = 1; rowEnd = 8; rowStep = +1;
            fileStart = 'h'; fileEnd = 'a'; fileStep = -1;
        }

        System.out.print("  ");
        for (char f = fileStart; ; f += fileStep) {
            System.out.print(" " + f + " ");
            if (f == fileEnd) break;
        }
        System.out.println();

        for (int r = rowStart; ; r += rowStep) {
            System.out.print(r + " ");
            for (char f = fileStart; ; f += fileStep) {
                int fileIndex = f - 'a' + 1;
                ChessPosition pos = new ChessPosition(r, fileIndex);
                ChessPiece p = board.getPiece(pos);

                boolean light = ((r + fileIndex) % 2 == 0);
                String bg = light
                        ? EscapeSequences.SET_BG_COLOR_LIGHT_GREY
                        : EscapeSequences.SET_BG_COLOR_DARK_GREY;

                String cell;
                if (p != null) {
                    String fg = p.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? EscapeSequences.SET_TEXT_COLOR_RED
                            : EscapeSequences.SET_TEXT_COLOR_BLUE;

                    String glyph;
                    switch (p.getPieceType()) {
                        case KING:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_KING
                                    : EscapeSequences.BLACK_KING;
                            break;
                        case QUEEN:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_QUEEN
                                    : EscapeSequences.BLACK_QUEEN;
                            break;
                        case ROOK:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_ROOK
                                    : EscapeSequences.BLACK_ROOK;
                            break;
                        case BISHOP:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_BISHOP
                                    : EscapeSequences.BLACK_BISHOP;
                            break;
                        case KNIGHT:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_KNIGHT
                                    : EscapeSequences.BLACK_KNIGHT;
                            break;
                        case PAWN:
                            glyph = (p.getTeamColor()==ChessGame.TeamColor.WHITE)
                                    ? EscapeSequences.WHITE_PAWN
                                    : EscapeSequences.BLACK_PAWN;
                            break;
                        default:
                            glyph = EscapeSequences.EMPTY;
                    }

                    cell = bg
                            + fg
                            + glyph
                            + EscapeSequences.RESET_TEXT_COLOR
                            + EscapeSequences.RESET_BG_COLOR;

                } else {
                    cell = bg
                            + EscapeSequences.EMPTY
                            + EscapeSequences.RESET_BG_COLOR;
                }

                System.out.print(cell);
                if (f == fileEnd) break;
            }

            System.out.println(" " + r);
            if (r == rowEnd) break;
        }

        System.out.print("  ");
        for (char f = fileStart; ; f += fileStep) {
            System.out.print(" " + f + " ");
            if (f == fileEnd) break;
        }
        System.out.println();
    }
}
