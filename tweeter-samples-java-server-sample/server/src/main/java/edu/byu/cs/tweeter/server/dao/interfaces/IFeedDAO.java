package edu.byu.cs.tweeter.server.dao.interfaces;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import edu.byu.cs.tweeter.util.Pair;

public interface IFeedDAO {
    Pair<List<Status>, Boolean> getFeed(String username, Status lastStatus, Integer limit);
    boolean postStatus(Status status, List<String> followers);

    void updateFeedBatch(Status status, List<String> followers);
}
