package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import chess.*;

import dataaccess.DataAccessException;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MySqlDataAccessTest {
    private MySqlDataAccess dao;

    @BeforeAll
    void setupSchema() throws DataAccessException {
        // make sure the database and tables exist
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
    }

    @BeforeEach
    void beforeEach() throws DataAccessException {
        dao = new MySqlDataAccess();
        dao.clear();
    }

    @Test
    @DisplayName("clear removes everything")
    void clearWorks() throws DataAccessException {
        dao.createUser(new UserData("u1","pass","u1@gmail.com"));
        dao.createAuth(new AuthData("t","u1"));
        GameData g = new GameData(0, null, null, "n", new ChessGame());
        int id = dao.createGame(g);

        dao.clear();

        assertNull(dao.getUser("u1"),     "users table should be empty");
        assertNull(dao.getAuth("t"),     "auth table should be empty");
        assertTrue(dao.listGames().isEmpty(), "games table should be empty");
        assertNull(dao.getGame(id),      "no game after clear");
    }

    @Test
    @DisplayName("createUser + getUser success")
    void createAndGetUser() throws DataAccessException {
        var u = new UserData("u2","pass","u2@mail");
        dao.createUser(u);
        var stored = dao.getUser("u2");
        assertNotNull(stored);
        assertEquals("u2",     stored.username());
        assertNotEquals("pass",     stored.password(),   "Password must be hashed");
        assertTrue( BCrypt.checkpw("pass", stored.password()), "Hash should match pw");
        assertEquals("u2@mail", stored.email());
    }

    @Test
    @DisplayName("createUser duplicate throws")
    void createUserDuplicate() {
        assertThrows(DataAccessException.class, () -> {
            dao.createUser(new UserData("u3","x","u3@gmail.com"));
            dao.createUser(new UserData("u3","y","u3@gmail.com"));
        });
    }

    @Test
    @DisplayName("getUser missing returns null")
    void getUserMissing() throws DataAccessException {
        assertNull(dao.getUser("no-such-user"));
    }

    @Test
    @DisplayName("createAuth + getAuth success")
    void createAndGetAuth() throws DataAccessException {
        dao.createUser(new UserData("u4","pw","u4@gmail.com"));
        var a = new AuthData("tok","u4");
        dao.createAuth(a);
        var stored = dao.getAuth("tok");
        assertNotNull(stored);
        assertEquals("tok",  stored.authToken());
        assertEquals("u4", stored.username());
    }

    @Test
    @DisplayName("createAuth duplicate throws")
    void createAuthDuplicate() throws DataAccessException {
        dao.createUser(new UserData("u5","pw","u5@gmail.com"));
        dao.createAuth(new AuthData("t1","u5"));
        assertThrows(DataAccessException.class, () ->
                dao.createAuth(new AuthData("t1","u5"))
        );
    }

    @Test
    @DisplayName("getAuth missing returns null")
    void getAuthMissing() throws DataAccessException {
        assertNull(dao.getAuth("nope"));
    }

    @Test
    @DisplayName("deleteAuth removes token")
    void deleteAuthRemoves() throws DataAccessException {
        dao.createUser(new UserData("u6","pw","u6@gmail.com"));
        dao.createAuth(new AuthData("t2","u6"));
        dao.deleteAuth("t2");
        assertNull(dao.getAuth("t2"));
    }

    @Test
    @DisplayName("createGame + getGame + listGames succeed (initial board)")
    void createGetListGame() throws DataAccessException {
        dao.createUser(new UserData("u7","pw","u7@gmail.com"));
        ChessGame initial = new ChessGame();
        GameData in = new GameData(0, null, null, "MyGame", initial);
        int id = dao.createGame(in);
        assertTrue(id > 0);

        GameData out = dao.getGame(id);
        assertNotNull(out);
        assertEquals(id,       out.gameID());
        assertEquals("MyGame", out.gameName());
        assertNull(out.whiteUsername());
        assertNull(out.blackUsername());
        assertEquals(initial, out.game());

        var all = dao.listGames();
        assertEquals(1, all.size());
        assertEquals(id, all.get(0).gameID());
    }

    @Test
    @DisplayName("createGame invalid data throws")
    void createGameInvalid() {
        assertThrows(DataAccessException.class, () ->
                dao.createGame(new GameData(0, null, null, null, new ChessGame()))
        );
    }

    @Test
    @DisplayName("getGame missing returns null")
    void getGameMissing() throws DataAccessException {
        assertNull(dao.getGame(99999));
    }

    @Test
    @DisplayName("listGames returns empty when no games")
    void listGamesEmpty() throws DataAccessException {
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    @DisplayName("updateGame success: set player and board")
    void updateGameSuccess() throws DataAccessException, InvalidMoveException {
        dao.createUser(new UserData("u8","pw","u8@gmail.com"));

        GameData before = new GameData(0, null, null, "Name", new ChessGame());
        int id = dao.createGame(before);
        GameData got = dao.getGame(id);

        got = got.withWhiteUsername("u8");
        dao.updateGame(got);
        GameData after = dao.getGame(id);
        assertEquals("u8", after.whiteUsername());

        ChessGame g = after.game();
        var move = new ChessMove(new ChessPosition(2,1), new ChessPosition(3,1), null);
        g.makeMove(move);
        after = after.withGame(g);
        dao.updateGame(after);

        GameData reloaded = dao.getGame(id);
        assertEquals(g, reloaded.game(), "Board after move should match");
    }

    @Test
    @DisplayName("updateGame nonexistent throws")
    void updateGameNonexistent() {
        GameData fake = new GameData(123, null, null, "n", new ChessGame());
        assertThrows(DataAccessException.class, () ->
                dao.updateGame(fake)
        );
    }
}
