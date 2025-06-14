package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.baseUrl   = "http://localhost:" + port;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void clearDatabase() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/db"))
                    .DELETE()
                    .build();
            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ServerException("Failed to clear DB: HTTP " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new ServerException("Could not clear database", e);
        }
    }

    public AuthData register(String username, String password, String email) {
        try {
            var body = gson.toJson(Map.of(
                    "username", username,
                    "password", password,
                    "email",    email
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/user"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ServerException("HTTP " + resp.statusCode() + ": " + resp.body());
            }
            return gson.fromJson(resp.body(), AuthData.class);
        } catch (ServerException se) {
            throw se;
        } catch (Exception e) {
            throw new ServerException("Failed to register", e);
        }
    }

    public int createGame(String authToken, String gameName) {
        try {
            var body = gson.toJson(Map.of("gameName", gameName));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/game"))
                    .header("Authorization", authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new ServerException("HTTP " + resp.statusCode() + ": " + resp.body());
            }

            Map<?, ?> json = gson.fromJson(resp.body(), Map.class);
            Number id = (Number) json.get("gameID");
            return id.intValue();
        } catch (Exception e) {
            throw new ServerException("Failed to create game", e);
        }
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

    public void joinGame(String authToken, int gameId, String color) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public GameData getGameState(String authToken, int gameId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
