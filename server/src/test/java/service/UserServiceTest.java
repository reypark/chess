package service;

import dataaccess.DataAccessException;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.user.MemoryUserDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.requests.LoginRequest;
import service.requests.LogoutRequest;
import service.requests.RegisterRequest;
import service.results.LoginResult;
import service.results.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserService service;

    @BeforeEach
    void setup() {
        service = new UserService(new MemoryUserDAO(), new MemoryAuthDAO());
    }

    @Test
    void clearSucceeds() {
        assertDoesNotThrow(() -> service.clear());
    }

    @Test
    void registerSucceeds() throws DataAccessException {
        RegisterResult r = service.register(new RegisterRequest("u1", "pw", "u1@mail"));
        assertEquals("u1", r.username());
        assertNotNull(r.authToken());
    }

    @Test
    void registerBadRequest() {
        var err = assertThrows(DataAccessException.class, () -> service.register(new RegisterRequest(null, "pw", "u2@mail")));
        assertEquals("bad request", err.getMessage());
    }

    @Test
    void loginSucceeds() throws DataAccessException {
        service.register(new RegisterRequest("u3", "pw", "u3@mail"));
        LoginResult r = service.login(new LoginRequest("u3", "pw"));
        assertEquals("u3", r.username());
        assertNotNull(r.authToken());
    }

    @Test
    void loginWrongPasswordFails() throws DataAccessException {
        service.register(new RegisterRequest("u4", "pw", "u4@mail"));
        var err = assertThrows(DataAccessException.class, () -> service.login(new LoginRequest("u4", "wp")));
        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void logoutInvalidatesToken() throws DataAccessException {
        RegisterResult r = service.register(new RegisterRequest("u5", "pw", "u5@mail"));
        String token = r.authToken();

        assertDoesNotThrow(() -> service.logout(new LogoutRequest(token)));
        var err = assertThrows(DataAccessException.class, () -> service.logout(new LogoutRequest(token)));
        assertTrue(err.getMessage().toLowerCase().contains("not found"));
    }

    @Test
    void logoutNullFails() {
        var err = assertThrows(DataAccessException.class, () -> service.logout(new LogoutRequest(null)));
        assertEquals("unauthorized", err.getMessage());
    }
}