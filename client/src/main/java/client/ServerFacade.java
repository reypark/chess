package client;

import model.AuthData;
import model.GameData;

import java.net.http.HttpClient;
import java.util.List;

public class ServerFacade {
    private final String baseUrl;
    private final HttpClient httpClient;

    public ServerFacade(int port) {
        this.baseUrl   = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
    }

    public AuthData register(String username, String password, String email) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public AuthData login(String username, String password) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void logout(String authToken) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<GameData> listGames(String authToken) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int createGame(String authToken, String gameName) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void joinGame(String authToken, int gameId, String color) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public GameData getGameState(String authToken, int gameId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
