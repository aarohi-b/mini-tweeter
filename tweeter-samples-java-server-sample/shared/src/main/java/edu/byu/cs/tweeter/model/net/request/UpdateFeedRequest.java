package edu.byu.cs.tweeter.model.net.request;

import java.util.List;
import edu.byu.cs.tweeter.model.domain.Status;

public class UpdateFeedRequest {
    public List<String> users;
    public Status status;

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    private UpdateFeedRequest() {}

    public UpdateFeedRequest(List<String> users, Status status) {
        this.users = users;
        this.status = status;
    }
}
