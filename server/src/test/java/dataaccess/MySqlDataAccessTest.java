package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import chess.*;

import org.mindrot.jbcrypt.BCrypt;

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
        dao.createUser(new UserData("u","p","e"));
        dao.createAuth(new AuthData("t","u"));
        GameData g = new GameData(0, null, null, "n", new ChessGame());
        int id = dao.createGame(g);

        dao.clear();

        assertNull(dao.getUser("u"),     "users table should be empty");
        assertNull(dao.getAuth("t"),     "auth table should be empty");
        assertTrue(dao.listGames().isEmpty(), "games table should be empty");
        assertNull(dao.getGame(id),      "no game after clear");
    }

    @Test
    @DisplayName("createUser + getUser success")
    void createAndGetUser() throws DataAccessException {
        var u = new UserData("alex","pw","alex@mail");
        dao.createUser(u);
        var stored = dao.getUser("alex");
        assertNotNull(stored);
        assertEquals("alex",     stored.username());
        assertNotEquals("pw",     stored.password(),   "Password must be hashed");
        assertTrue( BCrypt.checkpw("pw", stored.password()), "Hash should match pw");
        assertEquals("alex@mail", stored.email());
    }

    @Test
    @DisplayName("createUser duplicate throws")
    void createUserDuplicate() {
        assertThrows(DataAccessException.class, () -> {
            dao.createUser(new UserData("bob","x","b@b"));
            dao.createUser(new UserData("bob","y","c@c"));
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
        dao.createUser(new UserData("cam","pw","e"));
        var a = new AuthData("tok","cam");
        dao.createAuth(a);
        var stored = dao.getAuth("tok");
        assertNotNull(stored);
        assertEquals("tok",  stored.authToken());
        assertEquals("cam", stored.username());
    }

    @Test
    @DisplayName("createAuth duplicate throws")
    void createAuthDuplicate() throws DataAccessException {
        dao.createUser(new UserData("dan","pw","e"));
        dao.createAuth(new AuthData("t1","dan"));
        assertThrows(DataAccessException.class, () ->
                dao.createAuth(new AuthData("t1","dan"))
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
        dao.createUser(new UserData("eve","pw","e"));
        dao.createAuth(new AuthData("t2","eve"));
        dao.deleteAuth("t2");
        assertNull(dao.getAuth("t2"));
    }

    @Test
    @DisplayName("createGame + getGame + listGames succeed (initial board)")
    void createGetListGame() throws DataAccessException {
        dao.createUser(new UserData("g","p","e"));
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
        dao.createUser(new UserData("h","p","e"));

        // create and fetch
        GameData before = new GameData(0, null, null, "Name", new ChessGame());
        int id = dao.createGame(before);
        GameData got = dao.getGame(id);

        // 1) update just the player
        got = got.withWhiteUsername("h");
        dao.updateGame(got);
        GameData after = dao.getGame(id);
        assertEquals("h", after.whiteUsername());

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
