package server;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import spark.Request;
import spark.Response;
import spark.Spark;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.UUID;

public class Server {

    private final DataAccess dao;
    private final Gson gson = new Gson();

    public Server() {
        DataAccess tempDao = null;
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
            tempDao = new MySqlDataAccess();
        } catch (DataAccessException e) {
            System.err.println("FATAL: Failed to initialize Server: " + e.getMessage());
            e.printStackTrace();
            Spark.halt(500, gson.toJson(Map.of("message", "Error: Server initialization failed")));
        }
        this.dao = tempDao;
    }

    record CreateGameRequest(String gameName) {}
    record JoinGameRequest(String playerColor, Integer gameID) {}

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        setupClearDB();
        setupRegister();
        setupLogin();
        setupLogout();
        setupListGames();
        setupCreateGame();
        setupJoinGame();

        Spark.init();
        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void setupClearDB() {
        Spark.delete("/db", (req, res) -> {
            res.type("application/json");
            try {
                dao.clear();
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
                return null;
            }
        });
    }

    private void setupRegister() {
        Spark.post("/user", (req, res) -> {
            res.type("application/json");
            UserData user = parseOrBadRequest(req, res, UserData.class);
            if (user.username() == null || user.password() == null || user.email() == null) {
                Spark.halt(400, errorJson("bad request"));
            }

            try {
                dao.createUser(user);
            } catch (DataAccessException e) {
                String msg = e.getMessage().toLowerCase();
                if (msg.contains("taken")) {
                    Spark.halt(403, errorJson("already taken"));
                } else {
                    Spark.halt(500, errorJson(e.getMessage()));
                }
            }

            return getObject(res, user);
        });
    }

    private void setupLogin() {
        Spark.post("/session", (req, res) -> {
            res.type("application/json");
            UserData creds = parseOrBadRequest(req, res, UserData.class);
            if (creds.username() == null || creds.password() == null) {
                Spark.halt(400, errorJson("bad request"));
            }

            UserData stored;
            try {
                stored = dao.getUser(creds.username());
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
                return null;
            }

            if (stored == null || !BCrypt.checkpw(creds.password(), stored.password())) {
                Spark.halt(401, errorJson("unauthorized"));
            }

            return getObject(res, creds);
        });
    }

    private Object getObject(Response res, UserData creds) {
        String token = makeToken();
        try {
            dao.createAuth(new AuthData(token, creds.username()));
        } catch (DataAccessException e) {
            Spark.halt(500, errorJson(e.getMessage()));
        }

        res.status(200);
        return gson.toJson(Map.of("username", creds.username(), "authToken", token));
    }

    private void setupLogout() {
        Spark.delete("/session", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            try {
                dao.deleteAuth(token);
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
            }
            res.status(200);
            return "{}";
        });
    }

    private void setupListGames() {
        Spark.get("/game", (req, res) -> {
            res.type("application/json");
            extractAuthToken(req, res);
            try {
                var games = dao.listGames();
                res.status(200);
                return gson.toJson(Map.of("games", games));
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
                return null;
            }
        });
    }

    private void setupCreateGame() {
        Spark.post("/game", (req, res) -> {
            res.type("application/json");
            extractAuthToken(req, res);
            var body = parseOrBadRequest(req, res, CreateGameRequest.class);

            if (body.gameName() == null) {
                Spark.halt(400, errorJson("bad request"));
            }

            int newId;
            try {
                var toCreate = new GameData(0, null, null, body.gameName(), new ChessGame());
                newId = dao.createGame(toCreate);
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
                return null;
            }

            res.status(200);
            return gson.toJson(Map.of("gameID", newId));
        });
    }

    private void setupJoinGame() {
        Spark.put("/game", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            var body = parseOrBadRequest(req, res, JoinGameRequest.class);

            String color = body.playerColor();
            Integer gid = body.gameID();
            if (gid == null || color == null ||
                    !(color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("BLACK"))) {
                Spark.halt(400, errorJson("bad request"));
            }

            GameData game;
            try {
                game = dao.getGame(gid);
            } catch (DataAccessException e) {
                if (e.getMessage().toLowerCase().contains("not found")) {
                    Spark.halt(400, errorJson(e.getMessage()));
                } else {
                    Spark.halt(500, errorJson(e.getMessage()));
                }
                return null;
            }

            String me = dao.getAuth(token).username();
            if (color.equalsIgnoreCase("WHITE")) {
                if (game.whiteUsername() != null) {
                    Spark.halt(403, errorJson("already taken"));
                }
                game = game.withWhiteUsername(me);
            } else {
                if (game.blackUsername() != null) {
                    Spark.halt(403, errorJson("already taken"));
                }
                game = game.withBlackUsername(me);
            }

            try {
                dao.updateGame(game);
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
            }

            res.status(200);
            return "{}";
        });
    }

    private String extractAuthToken(Request req, Response res) {
        res.type("application/json");
        String token = req.headers("authorization");
        if (token == null || token.isBlank()) {
            Spark.halt(401, errorJson("unauthorized"));
        }
        try {
            if (dao.getAuth(token) == null) {
                Spark.halt(401, errorJson("unauthorized"));
            }
        } catch (DataAccessException e) {
            Spark.halt(500, errorJson(e.getMessage()));
        }
        return token;
    }

    private <T> T parseOrBadRequest(Request req, Response res, Class<T> cls) {
        try {
            T obj = gson.fromJson(req.body(), cls);
            if (obj == null) {
                throw new JsonSyntaxException("null");
            }
            return obj;
        } catch (JsonSyntaxException e) {
            res.status(400);
            res.type("application/json");
            Spark.halt(400, errorJson("bad request"));
            return null;
        }
    }

    private String errorJson(String msg) {
        return gson.toJson(Map.of("message", "Error: " + msg));
    }

    private static String makeToken() {
        return UUID.randomUUID().toString();
    }
}
