package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public class MySqlDataAccess implements DataAccess{
    public MySqlDataAccess()  {
    }

    @Override
    public void clear() throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void deleteAuth(String token) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        throw new DataAccessException("Not implemented yet");
    }
}
