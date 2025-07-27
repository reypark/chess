package dataaccess.auth;

import dataaccess.DataAccessException;
import dataaccess.user.SQLUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class SQLAuthDAOTest {
    private SQLUserDAO userDao;
    private SQLAuthDAO authDao;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new SQLUserDAO();
        authDao = new SQLAuthDAO();
        authDao.clear();
        userDao.clear();
    }

    @Test
    void createAuthSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u1", "pw1", "u1@mail"));
        assertDoesNotThrow(() ->
                authDao.createAuth(new AuthData("t1", "u1"))
        );
    }

    @Test
    void createAuthMissingUserFails() throws DataAccessException {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                authDao.createAuth(new AuthData("t2", "noUser"))
        );
        assertTrue(ex.getMessage().toLowerCase().contains("foreign"));
    }

    @Test
    void getAuthSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u2", "pw2", "u2@mail"));
        authDao.createAuth(new AuthData("t3", "u2"));
        AuthData a = authDao.getAuth("t3");
        assertEquals("t3", a.authToken());
        assertEquals("u2", a.username());
    }

    @Test
    void getAuthNotFoundFails() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                authDao.getAuth("noToken")
        );
        assertEquals("AuthToken not found", ex.getMessage());
    }

    @Test
    void deleteAuthSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u3", "pw3", "u3@mail"));
        authDao.createAuth(new AuthData("t4", "u3"));
        assertDoesNotThrow(() -> authDao.deleteAuth("t4"));
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                authDao.getAuth("t4")
        );
        assertEquals("AuthToken not found", ex.getMessage());
    }

    @Test
    void deleteAuthNotFoundFails() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                authDao.deleteAuth("bogus")
        );
        assertEquals("AuthToken not found", ex.getMessage());
    }

    @Test
    void clearSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u4", "pw4", "u4@mail"));
        authDao.createAuth(new AuthData("t5", "u4"));
        assertDoesNotThrow(() -> authDao.clear());
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                authDao.getAuth("t5")
        );
        assertEquals("AuthToken not found", ex.getMessage());
    }
}
