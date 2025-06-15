package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;
import chess.ChessGame;

import java.util.List;

public class GameService {
    private final DataAccess dao;

    public GameService(DataAccess dao) {
        this.dao = dao;
    }

    public int createGame(String name, String creator) throws DataAccessException {
        if (name == null || creator == null) {
            throw new DataAccessException("Bad request");
        }
        GameData gd = new GameData(
                0, null, null, name, new ChessGame()
        );
        return dao.createGame(gd);
    }

    public void joinGame(int gameId, String color, String user) throws DataAccessException {
        if (color == null || user == null) {
            throw new DataAccessException("Bad request");
        }
        GameData g = dao.getGame(gameId);
        if (g == null) {
            throw new DataAccessException("Game not found");
        }

        if (color.equalsIgnoreCase("WHITE")) {
            if (g.whiteUsername() != null) throw new DataAccessException("already taken");
            g = g.withWhiteUsername(user);
        } else if (color.equalsIgnoreCase("BLACK")) {
            if (g.blackUsername() != null) {
                throw new DataAccessException("already taken");
            }
            g = g.withBlackUsername(user);
        } else {
            throw new DataAccessException("Bad request");
        }

        dao.updateGame(g);
    }

    public List<GameData> listGames(String token) throws DataAccessException {
        AuthData a = dao.getAuth(token);
        if (a == null) {
            throw new DataAccessException("Unauthorized");
        }
        return dao.listGames();
    }
}
