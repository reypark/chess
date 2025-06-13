package model;

import chess.ChessGame;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) {
    public GameData withGame(ChessGame newGame) {
        return new GameData(
                this.gameID,
                this.whiteUsername,
                this.blackUsername,
                this.gameName,
                newGame
        );
    }

    public GameData withWhiteUsername(String newWhiteUsername) {
        return new GameData(
                this.gameID,
                newWhiteUsername,
                this.blackUsername,
                this.gameName,
                this.game
        );
    }

    public GameData withBlackUsername(String newBlackUsername) {
        return new GameData(
                this.gameID,
                this.whiteUsername,
                newBlackUsername,
                this.gameName,
                this.game
        );
    }
}
