package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

/**
 * CRUD operations for users.
 */
public interface UserDAO {
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
