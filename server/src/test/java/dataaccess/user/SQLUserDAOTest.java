package dataaccess.user;

import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import model.UserData;

import static org.junit.jupiter.api.Assertions.*;

class SQLUserDAOTest {
    private SQLUserDAO userDao;

    @BeforeEach
    void setUp() throws DataAccessException {
        userDao = new SQLUserDAO();
        userDao.clear();
    }

    @Test
    void createUserSucceeds() {
        assertDoesNotThrow(() ->
                userDao.createUser(new UserData("u1", "pw1", "u1@email.com"))
        );
    }

    @Test
    void createUserDuplicateFails() throws DataAccessException {
        userDao.createUser(new UserData("u2", "pw2", "u2@email.com"));
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userDao.createUser(new UserData("u2", "pwX", "u2b@email.com"))
        );
        assertTrue(ex.getMessage().startsWith("User already exists"));
    }

    @Test
    void getUserSucceeds() throws DataAccessException {
        var username = "u3";
        var password = "pw3";
        userDao.createUser(new UserData(username, password, "u3@email.com"));
        UserData found = userDao.getUser(username);
        assertEquals(username, found.username());
        assertTrue(BCrypt.checkpw(password, found.password()));
        assertEquals("u3@email.com", found.email());
    }

    @Test
    void getUserNotFoundFails() {
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userDao.getUser("noSuchUser")
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void clearSucceeds() throws DataAccessException {
        userDao.createUser(new UserData("u4", "pw4", "u4@email.com"));
        assertDoesNotThrow(() -> userDao.clear());
        DataAccessException ex = assertThrows(DataAccessException.class, () ->
                userDao.getUser("u4")
        );
        assertTrue(ex.getMessage().contains("not found"));
    }
}