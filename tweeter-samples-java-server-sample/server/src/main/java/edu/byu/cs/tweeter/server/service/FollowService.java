package edu.byu.cs.tweeter.server.service;

import java.util.ArrayList;
import java.util.List;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.*;
import edu.byu.cs.tweeter.model.net.response.*;
import edu.byu.cs.tweeter.server.dao.factory.IDAOAbstractFactory;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Contains the business logic for getting the users a user is following.
 */
public class FollowService extends Service{
    public FollowService(IDAOAbstractFactory f){
        super(f);
    }

    public FollowingResponse getFollowees(FollowingRequest request) {
        if(request.getFollowerAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a follower alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
            Pair<List<String>, Boolean> response = factory.getFollowDAO().getFollowees(request);
            List<User> followees = new ArrayList<>();

            for (String userAlias : response.getFirst()){
                followees.add(factory.getUserDAO().getUser(userAlias));
            }
            return new FollowingResponse(followees, response.getSecond());

        } else return new FollowingResponse("Auhtoken not valid: getFollowees()");
    }

    public FollowersResponse getFollowers(FollowersRequest request) {
        if(request.getFolloweeAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a followee alias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }
        System.out.println("Auhttoken: "+ request.getAuthToken());
        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
            Pair<List<String>, Boolean> response = factory.getFollowDAO().getFollowers(request);
            List<User> followers = new ArrayList<>();
            for (String userAlias : response.getFirst()){
                followers.add(factory.getUserDAO().getUser(userAlias));
            }
            return new FollowersResponse(followers, response.getSecond());

        } else return new FollowersResponse("Auhtoken not valid: getFollowers()");

    }

    public FollowingCountResponse getFollowingCount(FollowingCountRequest input) {
        if (factory.getAuthTokenDAO().validateAuthToken(input.getAuthToken())!=null) {
            int num = factory.getUserDAO().getFolloweeCount(input.getUsername());
            System.out.println("FOLLOWING COUNT "+ num);
            return new FollowingCountResponse(num);

        } else return new FollowingCountResponse("Failed authToken validation");
    }


    public FollowersCountResponse getFollowersCount(FollowersCountRequest input) {
        if (factory.getAuthTokenDAO(). validateAuthToken(input.getAuthToken())!=null) {
            int num = factory.getUserDAO().getFollowerCount(input.getUsername());
            System.out.println("FOLLOWER COUNT "+ num);
            return new FollowersCountResponse(num);

        } else return new FollowersCountResponse("Failed authToken validation");
    }

    public FollowResponse follow(FollowRequest followRequest) {
        if (factory.getAuthTokenDAO().validateAuthToken(followRequest.getAuth())!=null) {
            factory.getFollowDAO().follow(followRequest.getMyAlias(), followRequest.getFolloweeAlias());
            factory.getUserDAO().updateFolloweeCount(followRequest.getMyAlias(),1);
            factory.getUserDAO().updateFollowerCount(followRequest.getFolloweeAlias(), 1);
            return new FollowResponse();

        } else return new FollowResponse("Failed to Authenticate: follow()");
    }

    public UnfollowResponse unfollow(UnfollowRequest input) {
        if (factory.getAuthTokenDAO().validateAuthToken(input.getAuth())!=null) {
            factory.getFollowDAO().unfollow(input.getMyAlias(), input.getFolloweeAlias());
            factory.getUserDAO().updateFolloweeCount(input.getMyAlias(),-1);
            factory.getUserDAO().updateFollowerCount(input.getFolloweeAlias(), -1);
            return new UnfollowResponse();

        } else return new UnfollowResponse("Failed to Authenticate: unfollow()");
    }

    public IsFollowerResponse isFollower(IsFollowerRequest input) {
        if (factory.getAuthTokenDAO().validateAuthToken(input.getAuthToken())!=null) {
            boolean follows = factory.getFollowDAO().isFollowing(input.getFollowerAlias(), input.getFolloweeAlias());
            return new IsFollowerResponse(follows);
        } else return new IsFollowerResponse("Failed to Authenticate: isFollower");
    }
}
