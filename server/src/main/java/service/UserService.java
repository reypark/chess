package service;

import dataaccess.user.UserDAO;
import dataaccess.auth.AuthDAO;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import service.requests.*;
import service.results.*;

import java.util.UUID;

public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;

    public UserService(UserDAO userDao, AuthDAO authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public void clear() throws DataAccessException {
        authDao.clear();
        userDao.clear();
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        if (req.username()==null || req.password()==null || req.email()==null) {
            throw new DataAccessException("bad request");
        }
        boolean userExists = true;
        try {
            userDao.getUser(req.username());
        } catch (DataAccessException e) {
            userExists = false;
        }
        if (userExists) {
            throw new DataAccessException("User already exists: " + req.username());
        }

        userDao.createUser(new UserData(req.username(), req.password(), req.email()));
        String token = UUID.randomUUID().toString();
        authDao.createAuth(new AuthData(token, req.username()));
        return new RegisterResult(req.username(), token);
    }


    public LoginResult login(LoginRequest req) throws DataAccessException {
        if (req.username() == null || req.password() == null) {
            throw new DataAccessException("bad request");
        }
        UserData user;
        try {
            user = userDao.getUser(req.username());
        } catch (DataAccessException dbErr) {
            throw dbErr;
        }
        if (user == null || !user.password().equals(req.password())) {
            throw new DataAccessException("unauthorized");
        }
        String token = UUID.randomUUID().toString();
        authDao.createAuth(new AuthData(token, req.username()));
        return new LoginResult(req.username(), token);
    }

    public void logout(LogoutRequest req) throws DataAccessException {
        if (req.authToken() == null) {
            throw new DataAccessException("unauthorized");
        }
        authDao.deleteAuth(req.authToken());
    }
}