package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.MemoryUserDAO;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.CreateGameRequest;
import service.requests.JoinGameRequest;
import service.requests.RegisterRequest;
import service.results.CreateGameResult;
import service.results.RegisterResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameService service;
    private UserService users;
    private String token;

    @BeforeEach
    void setup() throws DataAccessException {
        var uDao = new MemoryUserDAO();
        var aDao = new MemoryAuthDAO();
        var gDao = new MemoryGameDAO();

        users = new UserService(uDao, aDao);
        service = new GameService(uDao, aDao, gDao);

        RegisterResult r = users.register(new RegisterRequest("u1", "pw", "u1@mail"));
        token = r.authToken();
    }

    @Test
    void clearSucceeds() {
        assertDoesNotThrow(() -> service.clear());
    }

    @Test
    void listEmpty() throws DataAccessException {
        List<GameData> list = service.listGames(token);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void listBadToken() {
        var err = assertThrows(DataAccessException.class,
                () -> service.listGames(token + "a"));
        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void createGameSucceeds() throws DataAccessException {
        CreateGameResult r = service.createGame(token, new CreateGameRequest("game1"));
        assertTrue(r.gameID() > 0);
    }

    @Test
    void createGameBadName() {
        var err = assertThrows(DataAccessException.class,
                () -> service.createGame(token, new CreateGameRequest("")));
        assertEquals("bad request", err.getMessage());
    }

    @Test
    void joinWhiteSucceeds() throws DataAccessException {
        int gameId = service.createGame(token, new CreateGameRequest("game2")).gameID();
        assertDoesNotThrow(() -> service.joinGame(token, new JoinGameRequest(ChessGame.TeamColor.WHITE.name(), gameId))
        );

        List<GameData> list = service.listGames(token);
        assertEquals(1, list.size());
        GameData game = list.get(0);
        assertEquals("u1", game.whiteUsername());
        assertNull(game.blackUsername());
    }

    @Test
    void joinTakenFails() throws DataAccessException {
        int gameId = service.createGame(token, new CreateGameRequest("game3")).gameID();
        service.joinGame(token,
                new JoinGameRequest(ChessGame.TeamColor.WHITE.name(), gameId));
        var err = assertThrows(DataAccessException.class,
                () -> service.joinGame(token,
                        new JoinGameRequest(ChessGame.TeamColor.WHITE.name(), gameId)));
        assertEquals("already taken", err.getMessage());
    }

    @Test
    void joinNullColorFails() throws DataAccessException {
        int gameId = service.createGame(token, new CreateGameRequest("game4")).gameID();
        var err = assertThrows(DataAccessException.class,
                () -> service.joinGame(token, new JoinGameRequest(null, gameId)));
        assertEquals("bad request", err.getMessage());
    }

    @Test
    void joinBadGameFails() {
        var err = assertThrows(DataAccessException.class,
                () -> service.joinGame(token,
                        new JoinGameRequest(ChessGame.TeamColor.WHITE.name(), 9999)));
        assertTrue(err.getMessage().startsWith("Game not found"));
    }
}