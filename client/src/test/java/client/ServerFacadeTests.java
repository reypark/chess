package client;

import dataaccess.DataAccessException;
import model.AuthData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

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
    public void register_SucceedsWithNewUser() {
        AuthData auth = facade.register("alex", "pw123", "alex@gmail.com");
        assertNotNull(auth);
        assertEquals("alex", auth.username());
        assertNotNull(auth.authToken());
        assertTrue(auth.authToken().length() > 10);
    }

    @Test
    public void register_FailsWhenUsernameTaken() {
        facade.register("bob", "password", "bob@gmail.com");
        ServerException ex = assertThrows(ServerException.class, () ->
                facade.register("bob", "other", "bob2@gmail.com")
        );
        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("403"), "Expected 403 in message, got: " + msg);
    }
}
