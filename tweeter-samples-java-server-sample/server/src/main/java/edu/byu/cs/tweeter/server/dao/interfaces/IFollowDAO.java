package edu.byu.cs.tweeter.server.dao.interfaces;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.util.Pair;

public interface IFollowDAO {
    Pair<List<String>, Boolean> getFollowers(FollowersRequest request);
    Pair<List<String>, Boolean> getFollowees(FollowingRequest request);

    void follow(String follower, String followee);

    List<String> getAllFollowers(AuthToken authToken, String followeeAlias);

    void unfollow(String userHandle, String unfollowedUserHandle);
    boolean isFollowing(String loggedInUser, String targetUser);

    void addFollowersBatch(List<String> followers, String followTarget);
//    List<User> getAllFollowers(String alias);
}
