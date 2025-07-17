package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserDAO implements UserDAO {
    private final ConcurrentHashMap<String, UserData> store = new ConcurrentHashMap<>();

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.username().isBlank()) {
            throw new DataAccessException("Invalid user data");
        }
        if (store.putIfAbsent(user.username(), user) != null) {
            throw new DataAccessException("User already exists: " + user.username());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = store.get(username);
        if (user == null) {
            throw new DataAccessException("User not found: " + username);
        }
        return user;
    }

    @Override
    public void clear() {
        store.clear();
    }
}
