package edu.byu.cs.tweeter.server.service;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.net.request.PostStatusRequest;
import edu.byu.cs.tweeter.model.net.request.UpdateFeedRequest;
import edu.byu.cs.tweeter.model.net.response.PostStatusResponse;
import edu.byu.cs.tweeter.server.dao.factory.IDAOAbstractFactory;

public class StatusService extends Service{

    public StatusService(IDAOAbstractFactory factory) {
        super(factory);
    }

    public PostStatusResponse postStatus(PostStatusRequest request) {

        if(request.getStatus().getUser() == null) {
            throw new RuntimeException("[Bad Request] Status needs to have a user");
        }

        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
//            List<String> followers = factory.getFollowDAO().getAllFollowers(request.getAuthToken(), request.getStatus().getUser().getAlias());
//            System.out.println("Here are the followers: "+followers.toString());
//            factory.getFeedDAO().postStatus(request.getStatus(), followers);
            factory.getStoryDAO().postStatus(request.getStatus());
            return new PostStatusResponse();
        } else return new PostStatusResponse("Failed to Authenticate: postStatus");

    }

    public void batchWriteStatus(UpdateFeedRequest request){
        List<String> followers = request.getUsers();
        Status status = request.getStatus();
        int batchSize = 25;

        for (int i = 0; i < followers.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, followers.size());
            List<String> batch = followers.subList(i, endIndex);
            factory.getFeedDAO().updateFeedBatch(status, batch);
        }
    }
}
