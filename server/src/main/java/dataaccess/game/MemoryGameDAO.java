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
    public GameData createGame(GameData game) {
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
    public List<GameData> listGames() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        int id = game.gameID();
        if (!store.containsKey(id)) {
            throw new DataAccessException("Cannot update, game not found: " + id);
        }
        store.put(id, game);
    }

    @Override
    public void clear() {
        store.clear();
        nextId = 1;
    }
}
