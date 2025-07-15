package dataaccess.game;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> store = new HashMap<>();
    private int nextId = 1;

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        int id = nextId++;
        GameData withId = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        store.put(id, withId);
        return withId;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData g = store.get(gameID);
        if (g == null) {
            throw new DataAccessException("Game not found: " + gameID);
        }
        return g;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return new ArrayList<>(store.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!store.containsKey(game.gameID())) {
            throw new DataAccessException("Cannot update, game not found: " + game.gameID());
        }
        store.put(game.gameID(), game);
    }

    @Override
    public void deleteGame(int gameID) throws DataAccessException {
        store.remove(gameID);
    }

    @Override
    public void clear() throws DataAccessException {
        store.clear();
        nextId = 1;
    }
}
