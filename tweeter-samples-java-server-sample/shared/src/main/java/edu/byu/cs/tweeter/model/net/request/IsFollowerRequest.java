package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public class IsFollowerRequest {
    private AuthToken authToken;
    private String followerAlias;
    private String followeeAlias;

    public IsFollowerRequest() {}

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getFollowerAlias() {
        return followerAlias;
    }

    public void setFollowerAlias(String followerAlias) {
        this.followerAlias = followerAlias;
    }

    public String getFolloweeAlias() {
        return followeeAlias;
    }

    public void setFolloweeAlias(String followeeAlias) {
        this.followeeAlias = followeeAlias;
    }

    public IsFollowerRequest(AuthToken authToken, String followerUsername, String followeeUsername) {
        this.authToken = authToken;
        this.followerAlias = followerUsername;
        this.followeeAlias = followeeUsername;
    }
}
