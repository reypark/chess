package dataaccess.user;

import dataaccess.DataAccessException;
import model.UserData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> store = new ConcurrentHashMap<>();

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.username().isEmpty()) {
            throw new DataAccessException("Invalid user data");
        }
        if (store.containsKey(user.username())) {
            throw new DataAccessException("User already exists: " + user.username());
        }
        store.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null || !store.containsKey(username)) {
            throw new DataAccessException("User not found: " + username);
        }
        return store.get(username);
    }
}
