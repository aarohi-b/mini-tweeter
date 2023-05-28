package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowersCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersCountResponse;

/**
 * Background task that queries how many followers a user has.
 */
public class GetFollowersCountTask extends GetCountTask {

    public GetFollowersCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(messageHandler, targetUser, authToken);
    }

    @Override
    protected int runCountTask() {
        FollowersCountRequest getFollowersCountRequest = new FollowersCountRequest(targetUser.getAlias(), authToken);
        try {
            FollowersCountResponse getFollowersCountResponse = new ServerFacade().getFollowersCount(getFollowersCountRequest, "/getfollowerscount");
            if (getFollowersCountResponse.isSuccess()) {
                return getFollowersCountResponse.getFollowersCount();
            }
            else {
                sendFailedMessage(getFollowersCountResponse.getMessage());
            }
        }
        catch (Exception e) {
            sendExceptionMessage(e);
        }
        return -1;
    }

}
