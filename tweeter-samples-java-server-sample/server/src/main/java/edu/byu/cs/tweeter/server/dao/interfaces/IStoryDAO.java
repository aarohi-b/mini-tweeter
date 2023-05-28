package edu.byu.cs.tweeter.server.dao.interfaces;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.Pair;

public interface IStoryDAO {
    Pair<List<Status>, Boolean> getStory(User user, Status lastStatus, int limit);

    boolean postStatus(Status status);
}
