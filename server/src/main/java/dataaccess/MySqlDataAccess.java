package dataaccess;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import chess.ChessGame;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySqlDataAccess implements DataAccess{
    private static final Gson GSON = new Gson();

    public MySqlDataAccess() throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM games");
            stmt.executeUpdate("DELETE FROM auth");
            stmt.executeUpdate("DELETE FROM users");
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear database", ex);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        final String sql = "INSERT INTO users(username, password, email) VALUES (?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String hash = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            ps.setString(1, user.username());
            ps.setString(2, hash);
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException ex) {
            if (ex instanceof SQLIntegrityConstraintViolationException
                    || ex.getErrorCode() == 1062
                    || "23000".equals(ex.getSQLState().substring(0,5))) {
                        throw new DataAccessException("already taken", ex);
            }
            throw new DataAccessException("unable to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        final String sql = "SELECT username, password, email FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new UserData(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to read user", ex);
        }
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        final String sql = "INSERT INTO auth(authToken, username) VALUES (?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("unable to create auth", ex);
        }
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        final String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthData(
                        rs.getString("authToken"),
                        rs.getString("username")
                );
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to read auth", ex);
        }
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        final String sql = "DELETE FROM auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("unable to delete auth", ex);
        }
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game.gameName() == null) {
            throw new DataAccessException("invalid game data");
        }
        final String sql = """
            INSERT INTO games
              (gameName, whiteUsername, blackUsername, gameState)
            VALUES (?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.gameName());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, GSON.toJson(game.game()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new DataAccessException("failed to fetch new game ID");
                }
                return keys.getInt(1);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        final String sql = "SELECT * FROM games WHERE gameID = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String name  = rs.getString("gameName");
                String wUser = rs.getString("whiteUsername");
                String bUser = rs.getString("blackUsername");
                String state = rs.getString("gameState");
                ChessGame g = GSON.fromJson(state, ChessGame.class);
                return new GameData(gameID, wUser, bUser, name, g);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to read game", ex);
        }
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        final String sql = "SELECT * FROM games";
        List<GameData> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int    id    = rs.getInt("gameID");
                String name  = rs.getString("gameName");
                String wUser = rs.getString("whiteUsername");
                String bUser = rs.getString("blackUsername");
                String state = rs.getString("gameState");
                ChessGame g  = GSON.fromJson(state, ChessGame.class);
                list.add(new GameData(id, wUser, bUser, name, g));
            }
            return list;
        } catch (SQLException ex) {
            throw new DataAccessException("unable to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        final String sql = """
            UPDATE games
               SET whiteUsername = ?,
                   blackUsername = ?,
                   gameState     = ?
             WHERE gameID       = ?
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, GSON.toJson(game.game()));
            ps.setInt(4, game.gameID());
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException("unable to update game", ex);
        }
    }
}
