package edu.byu.cs.tweeter.server.dao.interfaces;


import edu.byu.cs.tweeter.model.domain.AuthToken;

public interface IAuthTokenDAO {
    AuthToken generateToken();
    boolean deleteToken(AuthToken authToken);
    AuthToken getToken(String token);
    public AuthToken validateAuthToken(AuthToken authToken);
}
