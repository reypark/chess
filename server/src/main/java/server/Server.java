package server;

import spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

    private static String makeToken() {
        return UUID.randomUUID().toString();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", (req, res) -> {
            try {
                dao.clear();
                res.status(200);
                return "{}";
            } catch (DataAccessException e) {
                res.status(500);
                return "{ \"message\": \"Error: " + e.getMessage() + "\" }";
            }
        });

        Spark.post("/user", (req, res) -> {
            UserData request;
            try {
                request = gson.fromJson(req.body(), UserData.class);
            } catch (JsonSyntaxException ex) {
                res.status(400);
                return "{ \"message\": \"Error: bad request\" }";
            }
            if (request.username() == null
                    || request.password() == null
                    || request.email() == null) {
                res.status(400);
                return "{ \"message\": \"Error: bad request\" }";
            }
            try {
                dao.createUser(request);
            } catch (DataAccessException e) {
                res.status(403);
                return "{ \"message\": \"Error: already taken\" }";
            }
            String token = makeToken();
            dao.createAuth(new AuthData(token, request.username()));
            res.status(200);
            return gson.toJson(Map.of(
                    "username", request.username(),
                    "authToken", token
            ));
        });

        Spark.post("/session", (req, res) -> {
            UserData request;
            try {
                request = gson.fromJson(req.body(), UserData.class);
            } catch (JsonSyntaxException ex) {
                res.status(400);
                return "{ \"message\": \"Error: bad request\" }";
            }

            if (request.username() == null || request.password() == null) {
                res.status(400);
                return "{ \"message\": \"Error: bad request\" }";
            }

            UserData stored;
            try {
                stored = dao.getUser(request.username());
            } catch (DataAccessException e) {
                res.status(401);
                return "{ \"message\": \"Error: unauthorized\" }";
            }

            if (!stored.password().equals(request.password())) {
                res.status(401);
                return "{ \"message\": \"Error: unauthorized\" }";
            }

            String token = makeToken();
            dao.createAuth(new AuthData(token, request.username()));

            res.status(200);
            return gson.toJson(Map.of(
                    "username",  request.username(),
                    "authToken", token
            ));
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
}
