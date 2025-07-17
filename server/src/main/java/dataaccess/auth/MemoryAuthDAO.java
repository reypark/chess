package dataaccess.auth;

import model.AuthData;
import dataaccess.DataAccessException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final Map<String,AuthData> store = new ConcurrentHashMap<>();

    @Override
    public void createAuth(AuthData auth) {
        store.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        AuthData a = store.get(token);
        if (a == null) {
            throw new DataAccessException("AuthToken not found");
        }
        return a;
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        if (token == null || !store.containsKey(token)) {
            throw new DataAccessException("AuthToken not found");
        }
        store.remove(token);
    }

    @Override
    public void clear() {
        store.clear();
    }
}
