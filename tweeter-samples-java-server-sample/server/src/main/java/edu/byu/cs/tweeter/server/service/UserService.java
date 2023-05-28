package edu.byu.cs.tweeter.server.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.*;
import edu.byu.cs.tweeter.model.net.response.*;
import edu.byu.cs.tweeter.server.dao.factory.IDAOAbstractFactory;
import edu.byu.cs.tweeter.util.Pair;

public class UserService extends Service{
    public UserService(IDAOAbstractFactory factory) {
        super(factory);
    }

    public LoginResponse login(LoginRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        }

        String loginPassHash = getSecurePassword(request.getPassword());
        User user = factory.getUserDAO().login(request.getUsername(), loginPassHash);
        if (user!=null){
            AuthToken authToken = factory.getAuthTokenDAO().generateToken();
            return new LoginResponse(user, authToken);
        } else return new LoginResponse("Authentication failed: Invalid Password");
    }

    public RegisterResponse register(RegisterRequest request) {
        if(request.getUsername() == null){
            throw new RuntimeException("[Bad Request] Missing a username");
        } else if(request.getPassword() == null) {
            throw new RuntimeException("[Bad Request] Missing a password");
        } else if(request.getFirstName() == null) {
            throw new RuntimeException("[Bad Request] Missing a first name");
        } else if(request.getLastName() == null) {
            throw new RuntimeException("[Bad Request] Missing a last name");
        } else if(request.getImage() == null) {
            throw new RuntimeException("[Bad Request] Missing an image");
        }

        if (factory.getUserDAO().userAlreadyRegistered(request.getUsername())) {
            return new RegisterResponse(false, "This username already exists, try another");
        }else {
            User user = null;
            String securePassword = getSecurePassword(request.getPassword());
            try {
                String imageURL = factory.getUserDAO().putS3Image(request.getUsername(), request.getImage());
                user = factory.getUserDAO().register(request.getUsername(), securePassword, request.getFirstName(), request.getLastName(), imageURL);
            } catch (ParseException e) {
                e.printStackTrace();
                return new RegisterResponse(false, "Failed in UserService: register ");
            }
            AuthToken authToken = factory.getAuthTokenDAO().generateToken();
            setupFollowingList(request.getUsername());
            setupFollowerList(request.getUsername());

            return new RegisterResponse(user, authToken);
        }
    }

    public GetUserResponse getUser(GetUserRequest request) {
        if(request.getUsername() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a userAlias");
        }
        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
            User user = factory.getUserDAO().getUser(request.getUsername());
            return  new GetUserResponse(user);
        }
        else return new GetUserResponse("Failed authentication in UserService: getUSer");
    }

    public LogoutResponse logout(LogoutRequest request) {
        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null){
            factory.getAuthTokenDAO().deleteToken(request.getAuthToken());
            return new LogoutResponse();

        }
        else return new LogoutResponse("Failed authentication in UserService: logout ");
    }

    public StoryResponse getStory(StoryRequest request) {
        if(request.getAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a userAlias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
            User user = factory.getUserDAO().getUser(request.getAlias());
            Pair<List<Status>, Boolean> result = factory.getStoryDAO().getStory(user, request.getLastStatusTimeStamp(), request.getLimit());
            if(result != null){
                return new StoryResponse(result.getFirst(), result.getSecond());
            }
            return new StoryResponse("Failed in UserService: getStory");
        }
        return new StoryResponse("Failed to Authenticate in UserService: getStory");
    }

    public FeedResponse getFeed(FeedRequest request) {

        if(request.getAlias() == null) {
            throw new RuntimeException("[Bad Request] Request needs to have a userAlias");
        } else if(request.getLimit() <= 0) {
            throw new RuntimeException("[Bad Request] Request needs to have a positive limit");
        }

        if (factory.getAuthTokenDAO().validateAuthToken(request.getAuthToken())!=null) {
            Pair<List<Status>, Boolean> result = factory.getFeedDAO().getFeed(request.getAlias(), request.getLastStatus(), request.getLimit());
            if(result != null){
                return new FeedResponse(result.getFirst(), result.getSecond());
            }
            return new FeedResponse("Failed in UserService: getFeed");
        }
        return new FeedResponse("Failed to Authenticate in UserService: getFeed");
    }

    public void setupFollowingList(String userAlias){
        super.follow(userAlias, "@apple");
        super.follow(userAlias, "@grape");
        super.follow(userAlias, "@banana");
    }

    public void setupFollowerList (String userAlias){
        super.follow("@apple", userAlias);
        super.follow("@grape", userAlias);
        super.follow("@banana", userAlias);
    }
}


