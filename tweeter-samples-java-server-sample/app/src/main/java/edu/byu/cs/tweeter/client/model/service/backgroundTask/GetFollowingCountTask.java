package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Handler;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;

/**
 * Background task that queries how many other users a specified user is following.
 */
public class GetFollowingCountTask extends GetCountTask {
    public GetFollowingCountTask(AuthToken authToken, User targetUser, Handler messageHandler) {
        super(messageHandler, targetUser, authToken);
    }
    @Override
    protected int runCountTask() {

        FollowingCountRequest getFollowingCountRequest = new FollowingCountRequest(targetUser.getAlias(),
                authToken);
        try {
            FollowingCountResponse getFollowingCountResponse = new ServerFacade().
                    getFollowingCount(getFollowingCountRequest, "/getfollowingcount");
            if (getFollowingCountResponse.isSuccess()) {
                return getFollowingCountResponse.getFollowingCount();
            }
            else {
                sendFailedMessage(getFollowingCountResponse.getMessage());
            }
        }
        catch (Exception e) {
            sendExceptionMessage(e);
        }
        return -1;
    }

}
