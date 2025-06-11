package service;

import dataaccess.MockDataAccess;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceTest {
    private GameService  gsvc;
    private UserService  usvc;
    private MockDataAccess dao;
    private String       userToken;

    @BeforeEach
    void setup() throws DataAccessException {
        dao   = new MockDataAccess();
        usvc  = new UserService(dao);
        gsvc  = new GameService(dao);
        AuthData auth = usvc.register(new UserData("pk","pass","p@g.com"));
        userToken = auth.authToken();
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        int id = gsvc.createGame("FunGame", "pk");
        assertTrue(id > 0);
        GameData gd = dao.getGame(id);
        assertEquals("FunGame", gd.gameName());
    }

    @Test
    void createGameBadRequest() {
        assertThrows(DataAccessException.class,
                () -> gsvc.createGame(null, "pk"));
    }

    @Test
    void joinGameSuccess() throws DataAccessException {
        int id = gsvc.createGame("X", "pk");
        gsvc.joinGame(id, "WHITE", "pk");
        assertEquals("pk", dao.getGame(id).whiteUsername());
    }

    @Test
    void joinGameAlreadyTaken() throws DataAccessException {
        int id = gsvc.createGame("Y", "pk");
        gsvc.joinGame(id, "WHITE", "pk");
        assertThrows(DataAccessException.class,
                () -> gsvc.joinGame(id, "WHITE", "pk"));
    }

    @Test
    void joinGameNotFound() {
        assertThrows(DataAccessException.class,
                () -> gsvc.joinGame(999, "WHITE", "pk"));
    }

    @Test
    void joinGameBadColor() throws DataAccessException {
        int id = gsvc.createGame("Z", "pk");
        assertThrows(DataAccessException.class,
                () -> gsvc.joinGame(id, "GREEN", "pk"));
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(DataAccessException.class,
                () -> gsvc.listGames("badtoken"));
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        gsvc.createGame("A", "pk");
        gsvc.createGame("B", "pk");
        List<GameData> lst = gsvc.listGames(userToken);
        assertEquals(2, lst.size());
    }
}
