package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTaskUtils;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.FollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowingTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.IsFollowerTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.UnfollowTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.CountHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.IsFollowerHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.PagedNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.SimpleNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.CountObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.IsFollowerObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowService {
    public void findIfFollower(AuthToken authToken, User user, User followee, IsFollowerObserver observer){
        IsFollowerTask isFollowerTask = new IsFollowerTask(authToken, user, followee, new IsFollowerHandler(observer));
        BackgroundTaskUtils.runTask(isFollowerTask);
    }

    public void follow(AuthToken authToken, User user, User followee, SimpleNotificationObserver observer){
        FollowTask followTask = new FollowTask(authToken, user, followee, new SimpleNotifHandler(observer));
        BackgroundTaskUtils.runTask(followTask);
    }

    /**
     * Limits the number of followees returned and returns the next set of
     * followees after any that were returned in a previous request.
     * This is an asynchronous operation.
     *
     * @param authToken the session auth token.
     * @param targetUser the user for whom followees are being retrieved.
     * @param limit the maximum number of followees to return.
     * @param lastFollowee the last followee returned in the previous request (can be null).
     */
    public void getFollowees(AuthToken authToken, User targetUser, int limit, User lastFollowee, PagedObserver<User> observer) {
        GetFollowingTask followingTask = new GetFollowingTask(authToken, targetUser, limit, lastFollowee, new PagedNotifHandler<User>(observer));
        BackgroundTaskUtils.runTask(followingTask);
    }

    /**
     * Limits the number of followers returned and returns the next set of
     * followers after any that were returned in a previous request.
     * This is an asynchronous operation.
     *
     * @param authToken the session auth token.
     * @param targetUser the user for whom followers are being retrieved.
     * @param limit the maximum number of followers to return.
     * @param lastFollower the last follower returned in the previous request (can be null).
     */
    public void getFollowers(AuthToken authToken, User targetUser, int limit, User lastFollower, PagedObserver<User> observer) {
        GetFollowersTask followersTask = new GetFollowersTask(authToken, targetUser, limit, lastFollower, new PagedNotifHandler<User>(observer));
        BackgroundTaskUtils.runTask(followersTask);
    }

    public void unfollow(AuthToken authToken, User myUser, User followee, SimpleNotificationObserver observer){
        UnfollowTask unfollowTask = new UnfollowTask(authToken, myUser, followee, new SimpleNotifHandler(observer));
        BackgroundTaskUtils.runTask(unfollowTask);
    }

    public void getFollowersCount(AuthToken authToken, User user, CountObserver observer){
        GetFollowersCountTask getFollowersCountTask = new GetFollowersCountTask(authToken, user, new CountHandler(observer, true));
        BackgroundTaskUtils.runTask(getFollowersCountTask);
    }

    public void getFollowingCount(AuthToken authToken, User user, CountObserver observer){
        GetFollowingCountTask getFollowingCountTask = new GetFollowingCountTask(authToken, user, new CountHandler(observer, false));
        BackgroundTaskUtils.runTask(getFollowingCountTask);
    }
}
