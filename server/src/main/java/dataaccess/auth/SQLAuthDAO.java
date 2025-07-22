package dataaccess.auth;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLAuthDAO implements AuthDAO {
    private static final String INSERT_SQL =
            "INSERT INTO auth (auth_token, username) VALUES (?, ?)";
    private static final String SELECT_SQL =
            "SELECT auth_token, username FROM auth WHERE auth_token = ?";
    private static final String DELETE_SQL =
            "DELETE FROM auth WHERE auth_token = ?";
    private static final String CLEAR_SQL =
            "DELETE FROM auth";

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement insertAuthStatement = connection.prepareStatement(INSERT_SQL)) {

            insertAuthStatement.setString(1, auth.authToken());
            insertAuthStatement.setString(2, auth.username());
            insertAuthStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error creating auth token: " + auth.authToken(), sqlException);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement selectAuthStatement = connection.prepareStatement(SELECT_SQL)) {

            selectAuthStatement.setString(1, authToken);
            try (ResultSet resultSet = selectAuthStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new AuthData(
                            resultSet.getString("auth_token"),
                            resultSet.getString("username")
                    );
                }
                return null;
            }

        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error fetching auth token: " + authToken, sqlException);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement deleteAuthStatement = connection.prepareStatement(DELETE_SQL)) {

            deleteAuthStatement.setString(1, authToken);
            deleteAuthStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error deleting auth token: " + authToken, sqlException);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement clearAuthStatement = connection.prepareStatement(CLEAR_SQL)) {

            clearAuthStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error clearing auth table", sqlException);
        }
    }
}
