package client;

import model.AuthData;
import model.GameData;

import java.util.List;

public class ServerFacade {
    private final String baseUrl;

    public ServerFacade(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public AuthData register(String username, String password, String email) {
        throw new UnsupportedOperationException();
    }

    public AuthData login(String username, String password) {
        throw new UnsupportedOperationException();
    }

    public void logout(String authToken) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public List<GameData> listGames(String authToken) {
        throw new UnsupportedOperationException();
    }

    public GameData createGame(String authToken, String gameName) {
        throw new UnsupportedOperationException();
    }

    public GameData joinGame(String authToken, long gameId, boolean asWhite) {
        throw new UnsupportedOperationException();
    }

    public GameData observeGame(String authToken, long gameId) {
        throw new UnsupportedOperationException();
    }
}
