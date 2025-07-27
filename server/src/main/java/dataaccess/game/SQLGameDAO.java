package dataaccess.game;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private static final String[] createStatements = {
        """
        CREATE TABLE IF NOT EXISTS games (
          id INT NOT NULL AUTO_INCREMENT,
          white_username VARCHAR(50),
          black_username VARCHAR(50),
          game_name VARCHAR(255) NOT NULL,
          state_json TEXT NOT NULL,
          PRIMARY KEY (id),
          FOREIGN KEY (white_username) REFERENCES users(username) ON DELETE SET NULL,
          FOREIGN KEY (black_username) REFERENCES users(username) ON DELETE SET NULL
        ) 
        """
    };

    private final Gson gson = new Gson();

    public SQLGameDAO() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Unable to initialize games table", e);
        }
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (String stmt : createStatements) {
                try (var ps = conn.prepareStatement(stmt)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to configure games table: " + e.getMessage(), e);
        }
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (white_username, black_username, game_name, state_json) VALUES (?, ?, ?, ?)";
        String json = gson.toJson(game.game());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            if (game.whiteUsername() != null) {
                ps.setString(1, game.whiteUsername());
            } else ps.setNull(1, Types.VARCHAR); {}
            if (game.blackUsername() != null) {
                ps.setString(2, game.blackUsername());
            } else ps.setNull(2, Types.VARCHAR); {}
            ps.setString(3, game.gameName());
            ps.setString(4, json);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
                }
            }
            throw new DataAccessException("Failed to obtain generated game ID");
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage(), e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT id, white_username, black_username, game_name, state_json FROM games WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChessGame cg = gson.fromJson(rs.getString("state_json"), ChessGame.class);
                    return new GameData(
                            rs.getInt("id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"), cg);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage(), e);
        }
        throw new DataAccessException("Game not found: " + gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> list = new ArrayList<>();
        String sql = "SELECT id, white_username, black_username, game_name, state_json FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChessGame cg = gson.fromJson(rs.getString("state_json"), ChessGame.class);
                list.add(
                        new GameData(rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"), cg));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_username = ?, black_username = ?, state_json = ? WHERE id = ?";
        String json = gson.toJson(game.game());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (game.whiteUsername() != null) {
                ps.setString(1, game.whiteUsername());
            } else ps.setNull(1, Types.VARCHAR); {}
            if (game.blackUsername() != null) {
                ps.setString(2, game.blackUsername());
            } else ps.setNull(2, Types.VARCHAR); {}
            ps.setString(3, json);
            ps.setInt(4, game.gameID());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Cannot update, game not found: " + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage(), e);
        }
    }
}
