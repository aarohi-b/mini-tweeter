package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;

public class UnfollowRequest {
    private AuthToken auth;
    private String myAlias;
    private String followeeAlias;
    public UnfollowRequest(AuthToken authToken, String s1, String s2) {
        this.auth = authToken;
        this.myAlias = s1;
        this.followeeAlias = s2;
    }

    public UnfollowRequest() {}

    public AuthToken getAuth() {
        return auth;
    }

    public void setAuth(AuthToken auth) {
        this.auth = auth;
    }

    public String getMyAlias() {
        return myAlias;
    }

    public void setMyAlias(String myAlias) {
        this.myAlias = myAlias;
    }

    public String getFolloweeAlias() {
        return followeeAlias;
    }

    public void setFolloweeAlias(String followeeAlias) {
        this.followeeAlias = followeeAlias;
    }
}
