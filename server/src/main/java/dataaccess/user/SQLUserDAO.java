package dataaccess.user;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {
    private static final String INSERT_SQL =
            "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
    private static final String SELECT_SQL =
            "SELECT username, password, email FROM users WHERE username = ?";
    private static final String CLEAR_SQL =
            "DELETE FROM users";

    @Override
    public void createUser(UserData user) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL)) {

            insertStatement.setString(1, user.username());
            insertStatement.setString(2, user.password());
            insertStatement.setString(3, user.email());
            insertStatement.executeUpdate();

        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "User already exists: " + user.username(), sqlException);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(SELECT_SQL)) {

            selectStatement.setString(1, username);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserData(
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getString("email")
                    );
                }
                return null;
            }
        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error fetching user: " + username, sqlException);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement clearStatement = connection.prepareStatement(CLEAR_SQL)) {

            clearStatement.executeUpdate();
        } catch (SQLException sqlException) {
            throw new DataAccessException(
                    "Error clearing users table", sqlException);
        }
    }
}
