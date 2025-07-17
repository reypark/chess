package service;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

import chess.ChessGame;
import dataaccess.auth.AuthDAO;
import dataaccess.game.GameDAO;
import dataaccess.user.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.results.CreateGameResult;

public class GameService {
    private final UserDAO userDao;
    private final AuthDAO authDao;
    private final GameDAO gameDao;

    public GameService(UserDAO userDao, AuthDAO authDao, GameDAO gameDao) {
        this.userDao = userDao;
        this.authDao = authDao;
        this.gameDao = gameDao;
    }

    public void clear() throws DataAccessException {
        gameDao.clear();
        authDao.clear();
        userDao.clear();
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        try {
            authDao.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("unauthorized");
        }
        return gameDao.listGames().stream()
                .sorted(Comparator.comparingInt(GameData::gameID))
                .collect(Collectors.toList());
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest req)
            throws DataAccessException {
        AuthData auth = authDao.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }
        if (req.gameName() == null || req.gameName().isBlank()) {
            throw new DataAccessException("bad request");
        }

        GameData game = new GameData(
                0,
                null,
                null,
                req.gameName(),
                new ChessGame()
        );
        int newGameID = gameDao.createGame(game).gameID();
        return new CreateGameResult(newGameID);
    }

    public void joinGame(String authToken, JoinGameRequest req)
            throws DataAccessException {
        AuthData auth = authDao.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("unauthorized");
        }

        if (req.playerColor() == null) {
            throw new DataAccessException("bad request");
        }

        GameData existing = gameDao.getGame(req.gameID());
        if (existing == null) {
            throw new DataAccessException("bad request");
        }

        GameData updated;
        switch (req.playerColor()) {
            case "WHITE" -> {
                if (existing.whiteUsername() != null) {
                    throw new DataAccessException("already taken");
                }
                updated = new GameData(
                        existing.gameID(),
                        auth.username(),
                        existing.blackUsername(),
                        existing.gameName(),
                        existing.game()
                );
            }
            case "BLACK" -> {
                if (existing.blackUsername() != null) {
                    throw new DataAccessException("already taken");
                }
                updated = new GameData(
                        existing.gameID(),
                        existing.whiteUsername(),
                        auth.username(),
                        existing.gameName(),
                        existing.game()
                );
            }
            default -> throw new DataAccessException("bad request");
        }
        gameDao.updateGame(updated);
    }
}
