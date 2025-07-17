package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.auth.MemoryAuthDAO;
import dataaccess.game.MemoryGameDAO;
import dataaccess.user.MemoryUserDAO;
import service.GameService;
import service.UserService;
import service.requests.*;
import spark.*;

import java.util.Map;

public class Server {

    private final Gson gson = new Gson();
    private UserService userService;
    private GameService gameService;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        var authDao = new MemoryAuthDAO();
        var userDao = new MemoryUserDAO();
        var gameDao = new MemoryGameDAO();

        userService = new UserService(userDao, authDao);
        gameService = new GameService(userDao, authDao, gameDao);

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", "application/json", (req, res) -> {
            gameService.clear();
            res.status(200);
            res.type("application/json");
            return "{}";
        });

        Spark.post("/user", "application/json", (req, res) -> {
            RegisterRequest registerReq = gson.fromJson(req.body(), RegisterRequest.class);
            var result = userService.register(registerReq);
            res.status(200);
            res.type("application/json");
            return gson.toJson(result);
        });

        Spark.post("/session", "application/json", (req, res) -> {
            LoginRequest loginReq = gson.fromJson(req.body(), LoginRequest.class);
            var result = userService.login(loginReq);
            res.status(200);
            res.type("application/json");
            return gson.toJson(result);
        });

        Spark.delete("/session", "application/json", (req, res) -> {
            String token = req.headers("authorization");
            LogoutRequest logoutReq = new LogoutRequest(token);
            userService.logout(logoutReq);
            res.status(200);
            res.type("application/json");
            return "{}";
        });

        Spark.get("/game", "application/json", (req, res) -> {
            String token = req.headers("authorization");
            var games = gameService.listGames(token);
            res.status(200);
            res.type("application/json");
            return gson.toJson(Map.of("games", games));
        });

        Spark.post("/game", "application/json", (req, res) -> {
            String token = req.headers("authorization");
            CreateGameRequest createReq = gson.fromJson(req.body(), CreateGameRequest.class);
            var result = gameService.createGame(token, createReq);
            res.status(200);
            res.type("application/json");
            return gson.toJson(result);
        });

        Spark.put("/game", "application/json", (req, res) -> {
            String token = req.headers("authorization");
            JoinGameRequest joinReq = gson.fromJson(req.body(), JoinGameRequest.class);
            gameService.joinGame(token, joinReq);
            res.status(200);
            res.type("application/json");
            return "{}";
        });

        registerExceptionHandlers();

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private void registerExceptionHandlers() {
        Spark.exception(DataAccessException.class, (ex, req, res) -> {
            String msg = ex.getMessage();
            int status;
            if ("bad request".equals(msg) || msg.startsWith("Game not found")) {
                status = 400;
            } else if ("unauthorized".equals(msg)
                    || msg.startsWith("User not found")
                    || msg.startsWith("AuthToken not found")) {
                status = 401;
            } else if ("already taken".equals(msg)
                    || msg.startsWith("User already exists")) {
                status = 403;
            } else {
                status = 500;
            }
            res.status(status);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: " + msg)));
        });

        Spark.exception(Exception.class, (ex, req, res) -> {
            res.status(500);
            res.type("application/json");
            res.body(gson.toJson(Map.of("message", "Error: " + ex.getMessage())));
        });
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
