package client;

import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private static Server server;
    private static ServerFacade facade;
    private String token;

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDb() {
        facade.clearDatabase();
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @Test
    void registerSuccess() {
        AuthData a = facade.register("u1","pw","u@gmail.com");
        assertNotNull(a.authToken());
        assertEquals("u1", a.username());
    }

    @Test
    void registerDuplicateFails() {
        facade.register("u2","pw","u2@gmail.com");
        Exception ex = assertThrows(RuntimeException.class, () ->
                facade.register("u2","pw2","u@gmail.com")
        );
        assertTrue(ex.getMessage().contains("403") || ex.getMessage().toLowerCase().contains("taken"));
    }

    @Test
    void loginSuccess() {
        facade.register("u3","pw","u3@gmail.com");
        AuthData a = facade.login("u3","pw");
        assertEquals("u3", a.username());
    }

    @Test
    void loginBadCredentialsFails() {
        facade.register("u4","pw","u4@gmail.com");
        assertThrows(RuntimeException.class, () -> facade.login("u4","wp"));
    }

    @Test
    void logoutSuccess() {
        AuthData a = facade.register("u5","pw","u5@gmail.com");
        facade.logout(a.authToken());
        assertThrows(RuntimeException.class, () -> facade.listGames(a.authToken()));
    }

    @Test @Order(6)
    void createAndListGames() {
        AuthData a = facade.register("u6","pw","u6@gmail.com");
        int id = facade.createGame(a.authToken(),"MyGame");
        assertTrue(id > 0);
        List<GameData> games = facade.listGames(a.authToken());
        assertEquals(1, games.size());
        assertEquals("MyGame", games.get(0).gameName());
    }

    @Test
    void joinGameSuccess() {
        AuthData a = facade.register("u7","pw","u7@gmail.com");
        int id = facade.createGame(a.authToken(),"G");
        facade.joinGame(id,"WHITE", a.authToken());
        GameData g = facade.getGameState(a.authToken(), id);
        assertEquals("u7", g.whiteUsername());
    }

    @Test
    void joinGameInvalidColorFails() {
        AuthData a = facade.register("u8","pw","u8@gmail.com");
        int id = facade.createGame(a.authToken(),"G2");
        assertThrows(RuntimeException.class, () -> facade.joinGame(id,"PURPLE", a.authToken()));
    }

    @Test
    void listGamesUnauthorizedFails() {
        assertThrows(RuntimeException.class, () -> facade.listGames("bad-token"));
    }
}
