package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.AuthData;
import model.GameData;

import java.lang.reflect.Type;
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

    public AuthData login(String username, String password) {
        try {
            var body = gson.toJson(Map.of(
                    "username", username,
                    "password", password
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/session"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
            }
            return gson.fromJson(resp.body(), AuthData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to login", e);
        }
    }

    public void logout(String authToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/session"))
                    .header("authorization", authToken)
                    .DELETE()
                    .build();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to logout", e);
        }
    }

    public List<GameData> listGames(String authToken) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/game"))
                    .header("authorization", authToken)
                    .GET()
                    .build();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode());
            }
            Type gamesListType = new TypeToken<Map<String, List<GameData>>>(){}.getType();
            Map<String,List<GameData>> map = gson.fromJson(resp.body(), gamesListType);
            return map.get("games");
        } catch (Exception e) {
            throw new RuntimeException("Failed to list games", e);
        }
    }

    public int createGame(String authToken, String gameName) {
        try {
            var body = gson.toJson(Map.of("gameName", gameName));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/game"))
                    .header("authorization", authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode());
            }
            Map<String,Double> map = gson.fromJson(resp.body(), Map.class);
            return map.get("gameID").intValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create game", e);
        }
    }

    public void joinGame(int gameId, String color, String authToken) {
        try {
            var body = gson.toJson(Map.of(
                    "gameID",     gameId,
                    "playerColor", color
            ));
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/game"))
                    .header("authorization", authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("HTTP " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to join game", e);
        }
    }

//    public GameData observeGame(String authToken, int gameId) {
//        return getGameState(authToken, gameId);
//    }

    public GameData getGameState(String authToken, int gameId) {
        return listGames(authToken)
                .stream()
                .filter(g -> g.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Game not found"));
    }
}
