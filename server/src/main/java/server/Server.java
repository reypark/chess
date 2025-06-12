package server;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.MySqlDataAccess;
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

    public Server() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        this.dao = new MySqlDataAccess();
    }

    record CreateGameRequest(String gameName) {}
    record JoinGameRequest(String playerColor, Integer gameID) {}

    private static String makeToken() {
        return UUID.randomUUID().toString();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

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

        Spark.post("/user", (req, res) -> {
            res.type("application/json");
            UserData user = parseOrBadRequest(req, res, UserData.class);

            if (user.username()==null || user.password()==null || user.email()==null) {
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

            String token = makeToken();
            try {
                dao.createAuth(new AuthData(token, user.username()));
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
            }

            res.status(200);
            return gson.toJson(Map.of(
                    "username",  user.username(),
                    "authToken", token
            ));
        });

        Spark.post("/session", (req, res) -> {
            res.type("application/json");
            UserData creds = parseOrBadRequest(req, res, UserData.class);

            if (creds.username()==null || creds.password()==null) {
                Spark.halt(400, errorJson("bad request"));
            }

            UserData stored;
            try {
                stored = dao.getUser(creds.username());
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
                return null;
            }

            if (stored==null
                    || !BCrypt.checkpw(creds.password(), stored.password())) {
                Spark.halt(401, errorJson("unauthorized"));
            }

            String token = makeToken();
            try {
                dao.createAuth(new AuthData(token, creds.username()));
            } catch (DataAccessException e) {
                Spark.halt(500, errorJson(e.getMessage()));
            }

            res.status(200);
            return gson.toJson(Map.of(
                    "username",  creds.username(),
                    "authToken", token
            ));
        });

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

        Spark.post("/game", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            var body = parseOrBadRequest(req, res, CreateGameRequest.class);

            if (body.gameName()==null) {
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

        Spark.put("/game", (req, res) -> {
            res.type("application/json");
            String token = extractAuthToken(req, res);
            var body = parseOrBadRequest(req, res, JoinGameRequest.class);

            String color = body.playerColor();
            Integer gid   = body.gameID();
            if (gid==null
                    || color==null
                    || !(color.equalsIgnoreCase("WHITE")||color.equalsIgnoreCase("BLACK"))) {
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
                if (game.whiteUsername()!=null) {
                    Spark.halt(403, errorJson("already taken"));
                }
                game = game.withWhiteUsername(me);
            } else {
                if (game.blackUsername()!=null) {
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
            if (obj == null) throw new JsonSyntaxException("null");
            return obj;
        } catch (JsonSyntaxException e) {
            res.status(400);
            res.type("application/json");
            String err = errorJson("bad request");
            Spark.halt(400, err);
            return null;
        }
    }

    private String errorJson(String msg) {
        return gson.toJson(Map.of("message", "Error: " + msg));
    }
}
