package server;

import chess.ChessGame;
import model.GameData;
import spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import model.UserData;
import model.AuthData;
import dataaccess.DataAccess;
import dataaccess.MockDataAccess;
import dataaccess.DataAccessException;

public class Server {

    private final DataAccess dao = new MockDataAccess();
    private final Gson gson = new Gson();

    record CreateGameRequest(String gameName) {}
    record JoinGameRequest(String playerColor, Integer gameID) {}

    private static String makeToken() {
        return UUID.randomUUID().toString();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) -> {
            res.type("application/json");
            try {
                dao.clear();
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                res.status(500);
                return errorJson(e.getMessage());
            }
        });

        Spark.post("/user", (req, res) -> {
            res.type("application/json");
            UserData user = parseOrBadRequest(req, res, UserData.class);
            if (user == null) return res.body();

            if (user.username() == null || user.password() == null || user.email() == null) {
                res.status(400);
                return errorJson("bad request");
            }

            try {
                dao.createUser(user);
            } catch (DataAccessException e) {
                res.status(403);
                return errorJson("already taken");
            }

            String token = makeToken();
            dao.createAuth(new AuthData(token, user.username()));

            res.status(200);
            return gson.toJson(Map.of(
                    "username",  user.username(),
                    "authToken", token
            ));
        });

        Spark.post("/session", (req, res) -> {
            res.type("application/json");
            UserData creds = parseOrBadRequest(req, res, UserData.class);
            if (creds == null) return res.body();

            if (creds.username() == null || creds.password() == null) {
                res.status(400);
                return errorJson("bad request");
            }

            UserData stored;
            try {
                stored = dao.getUser(creds.username());
            } catch (DataAccessException e) {
                res.status(401);
                return errorJson("unauthorized");
            }
            if (stored == null || !stored.password().equals(creds.password())) {
                res.status(401);
                return errorJson("unauthorized");
            }

            String token = makeToken();
            dao.createAuth(new AuthData(token, creds.username()));

            res.status(200);
            return gson.toJson(Map.of(
                    "username",  creds.username(),
                    "authToken", token
            ));
        });

        Spark.delete("/session", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            if (token == null) return res.body();

            try {
                dao.deleteAuth(token);
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                res.status(500);
                return errorJson(e.getMessage());
            }
        });

        Spark.get("/game", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            if (token == null) return res.body();

            try {
                List<GameData> games = dao.listGames();
                res.status(200);
                return gson.toJson(Map.of("games", games));
            } catch (DataAccessException e) {
                res.status(500);
                return errorJson(e.getMessage());
            }
        });

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private String extractAuthToken(Request req, Response res) {
        res.type("application/json");
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            res.status(401);
            res.body(errorJson("unauthorized"));
            return null;
        }

        try {
            dao.getAuth(token);
        } catch (DataAccessException e) {
            res.status(401);
            res.body(errorJson("unauthorized"));
            return null;
        }
        return token;
    }

    private <T> T parseOrBadRequest(Request req, Response res, Class<T> cls) {
        try {
            T obj = gson.fromJson(req.body(), cls);
            if (obj == null) throw new JsonSyntaxException("null");
            return obj;
        } catch (JsonSyntaxException e) {
            res.status(400);
            res.type("application/json");
            String body = errorJson("bad request");
            Spark.halt(400, body);
            return null;
        }
    }

    private String errorJson(String msg) {
        return gson.toJson(Map.of("message", "Error: " + msg));
    }
}
