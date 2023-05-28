package edu.byu.cs.tweeter.model.net.request;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;

public class StoryRequest {
    private AuthToken authToken;
    private String alias;
    private int limit;
    public Status lastStatus;

    public StoryRequest() {}

    public AuthToken getAuthToken() {
        return authToken;
    }

    public void setAuthToken(AuthToken authToken) {
        this.authToken = authToken;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Status getLastStatusTimeStamp() {
        return lastStatus;
    }

    public void setLastStatusTimeStamp(Status lastStatus) {
        this.lastStatus = lastStatus;
    }

    public StoryRequest(AuthToken authToken, String username, int limit, Status lastStatus) {
        this.authToken = authToken;
        this.alias = username;
        this.limit = limit;
        this.lastStatus = lastStatus;
    }
}
