package dataaccess.game;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.user.SQLUserDAO;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOTest {
    private SQLUserDAO userDao;
    private SQLGameDAO gameDao;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new SQLUserDAO();
        gameDao = new SQLGameDAO();
        userDao.clear();
        gameDao.clear();
    }

    @Test
    void createGameSucceeds() throws DataAccessException {
        GameData g = new GameData(0, null, null, "g1", new ChessGame());
        GameData inserted = gameDao.createGame(g);
        assertTrue(inserted.gameID() > 0);
        assertEquals("g1", inserted.gameName());
    }

    @Test
    void createGameBadNameFails() {
        GameData bad = new GameData(0, null, null, null, new ChessGame());
        assertThrows(DataAccessException.class, () ->
                gameDao.createGame(bad)
        );
    }

    @Test
    void getGameSucceeds() throws DataAccessException {
        GameData g = new GameData(0, null, null, "g2", new ChessGame());
        int id = gameDao.createGame(g).gameID();
        GameData found = gameDao.getGame(id);
        assertEquals(id, found.gameID());
        assertEquals("g2", found.gameName());
    }

    @Test
    void getGameNotFoundFails() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameDao.getGame(9999)
        );
        assertTrue(ex.getMessage().startsWith("Game not found"));
    }

    @Test
    void listGamesEmpty() throws DataAccessException {
        List<GameData> list = gameDao.listGames();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void listGamesNonEmpty() throws DataAccessException {
        int id1 = gameDao.createGame(new GameData(0, null, null, "g3", new ChessGame())).gameID();
        int id2 = gameDao.createGame(new GameData(0, null, null, "g4", new ChessGame())).gameID();
        List<GameData> list = gameDao.listGames();
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(g -> g.gameID() == id1));
        assertTrue(list.stream().anyMatch(g -> g.gameID() == id2));
    }

    @Test
    void updateGameSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u1", "pw", "u1@mail"));
        GameData original = gameDao.createGame(new GameData(0, null, null, "g5", new ChessGame()));
        GameData updated = new GameData(
                original.gameID(),
                "u1",
                null,
                original.gameName(),
                original.game()
        );
        assertDoesNotThrow(() -> gameDao.updateGame(updated));
        GameData fetched = gameDao.getGame(original.gameID());
        assertEquals("u1", fetched.whiteUsername());
    }

    @Test
    void updateGameNotFoundFails() {
        GameData bad = new GameData(9999, null, null, "no", new ChessGame());
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                gameDao.updateGame(bad)
        );
        assertTrue(ex.getMessage().startsWith("Cannot update"));
    }

    @Test
    void clearSucceeds() throws DataAccessException {
        gameDao.createGame(new GameData(0, null, null, "g6", new ChessGame()));
        assertDoesNotThrow(() -> gameDao.clear());
        List<GameData> list = gameDao.listGames();
        assertTrue(list.isEmpty());
    }
}
