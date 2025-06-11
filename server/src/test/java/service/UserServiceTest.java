package service;

import dataaccess.MockDataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService svc;

    @BeforeEach
    void setup() {
        svc = new UserService(new MockDataAccess());
    }

    @Test
    void registerSuccess() throws DataAccessException {
        UserData u = new UserData("alex","pw","a@g.com");
        AuthData auth = svc.register(u);
        assertEquals("alex", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void registerDuplicateFails() throws DataAccessException {
        UserData u = new UserData("bob","pw","b@g.com");
        svc.register(u);
        assertThrows(DataAccessException.class, () -> svc.register(u));
    }

    @Test
    void loginSuccess() throws DataAccessException {
        UserData u = new UserData("chris","pw","c@g.com");
        svc.register(u);
        AuthData auth = svc.login("chris","pw");
        assertEquals("chris", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void loginUnauthorized() {
        assertThrows(DataAccessException.class,
                () -> svc.login("nonexistent","pw"));
    }

    @Test
    void logoutRemovesToken() throws DataAccessException {
        UserData u = new UserData("dan","pw","d@b.com");
        AuthData auth = svc.register(u);
        svc.logout(auth.authToken());
        // second logout should now fail
        assertThrows(DataAccessException.class,
                () -> svc.logout(auth.authToken()));
    }
}
