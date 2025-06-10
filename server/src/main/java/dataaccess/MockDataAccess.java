package dataaccess;

import model.UserData;
import model.AuthData;
import model.GameData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MockDataAccess implements DataAccess {
    private final Map<String, UserData> users = new HashMap<>();
    private final Map<String, AuthData> auths = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger nextGameID = new AtomicInteger(1);

    @Override
    public void clear() {
        users.clear();
        auths.clear();
        games.clear();
        nextGameID.set(1);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null
                || user.password() == null || user.email() == null) {
            throw new DataAccessException("Missing user fields");
        }
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Username already taken");
        }
        users.put(user.username(), user);
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null || auth.authToken() == null || auth.username() == null) {
            throw new DataAccessException("Invalid auth data");
        }
        auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String token) {
        return auths.get(token);
    }

    @Override
    public void deleteAuth(String token) {
        auths.remove(token);
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null || game.gameName() == null) {
            throw new DataAccessException("Invalid game data");
        }
        int id = nextGameID.getAndIncrement();
        GameData toStore = new GameData(id,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game());
        games.put(id, toStore);
        return id;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null || !games.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found");
        }
        games.put(game.gameID(), game);
    }
}
