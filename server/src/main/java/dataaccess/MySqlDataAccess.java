package dataaccess;

import com.google.gson.Gson;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlDataAccess implements DataAccess{
    private final Gson gson = new Gson();

    public MySqlDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM users");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear tables", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null
                || user.username() == null
                || user.password() == null
                || user.email()    == null) {
            throw new DataAccessException("Missing user fields");
        }
        String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, hashed);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (ex.getSQLState().startsWith("23")) {
                throw new DataAccessException("Username already taken", ex);
            }
            throw new DataAccessException("failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get user", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null
                || auth.authToken() == null
                || auth.username()  == null) {
            throw new DataAccessException("Invalid auth data");
        }
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get auth", ex);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete auth", ex);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null || game.gameName() == null || game.game() == null) {
            throw new DataAccessException("Invalid game data");
        }
        String sql = """
            INSERT INTO games (gameName, whiteUsername, blackUsername, gameState)
             VALUES (?, ?, ?, ?)
          """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.gameName());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, gson.toJson(game.game()));
            ps.executeUpdate();

            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    throw new DataAccessException("failed to fetch gameID");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = """
            SELECT gameID, whiteUsername, blackUsername, gameName, gameState
              FROM games WHERE gameID = ?
          """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Game not found");
                }
                ChessGame cg = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        cg
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get game", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        String sql = """
            SELECT gameID, whiteUsername, blackUsername, gameName, gameState
              FROM games
          """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            List<GameData> out = new ArrayList<>();
            while (rs.next()) {
                ChessGame cg = gson.fromJson(rs.getString("gameState"), ChessGame.class);
                out.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        cg
                ));
            }
            return out;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) throw new DataAccessException("Invalid game data");
        String sql = """
            UPDATE games
               SET whiteUsername = ?, blackUsername = ?, gameState = ?
             WHERE gameID = ?
          """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, gson.toJson(game.game()));
            ps.setInt(4, game.gameID());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to update game", ex);
        }
    }
}
