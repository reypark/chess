package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

import java.util.UUID;

public class UserService {
    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public AuthData register(UserData u) throws DataAccessException {
        if (u == null
                || u.username() == null
                || u.password() == null
                || u.email() == null) {
            throw new DataAccessException("Missing fields");
        }
        dao.createUser(u);
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, u.username());
        dao.createAuth(auth);
        return auth;
    }

    public AuthData login(String username, String password) throws DataAccessException {
        if (username == null || password == null) {
            throw new DataAccessException("Missing credentials");
        }
        UserData stored = dao.getUser(username);
        if (stored == null || !stored.password().equals(password)) {
            throw new DataAccessException("Unauthorized");
        }
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData(token, username);
        dao.createAuth(auth);
        return auth;
    }

    public void logout(String token) throws DataAccessException {
        if (token == null) throw new DataAccessException("Unauthorized");
        AuthData auth = dao.getAuth(token);
        if (auth == null) throw new DataAccessException("Unauthorized");
        dao.deleteAuth(token);
    }
}
