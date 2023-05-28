package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTaskUtils;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFeedTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetStoryTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetUserTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.AuthHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.GetUserHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.PagedNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.SimpleNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.AuthObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.GetUserObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {

    public void login(String username, String password, AuthObserver observer){
        // Send the login request.
        LoginTask loginTask = new LoginTask(username, password, new AuthHandler(observer));
        BackgroundTaskUtils.runTask(loginTask);
    }

    public void register(String firstName, String lastName, String username, String password, String image, AuthObserver observer){
        // Send the login request.
        RegisterTask registerTask = new RegisterTask(firstName, lastName, username, password, image, new AuthHandler(observer));
        BackgroundTaskUtils.runTask(registerTask);
    }

    public void logout(AuthToken authToken, SimpleNotificationObserver observer){
        LogoutTask logoutTask = new LogoutTask(authToken, new SimpleNotifHandler(observer));
        BackgroundTaskUtils.runTask(logoutTask);
    }

    public void getUser(AuthToken authToken, String alias, GetUserObserver getUserObserver) {
        GetUserTask getUserTask = new GetUserTask(authToken, alias, new GetUserHandler(getUserObserver));
        BackgroundTaskUtils.runTask(getUserTask);
    }

    public void getFeed(AuthToken authToken, User user, int pageSize, Status lastStatus, PagedObserver getFeedObserver){
        GetFeedTask getFeedTask = new GetFeedTask(authToken, user, pageSize, lastStatus, new PagedNotifHandler<Status>(getFeedObserver));
        BackgroundTaskUtils.runTask(getFeedTask);
    }
    public void getStory(AuthToken currUserAuthToken, User user, int pageSize, Status lastStatus, PagedObserver<Status> getStoryObserver) {
        GetStoryTask getStoryTask = new GetStoryTask(currUserAuthToken, user, pageSize, lastStatus, new PagedNotifHandler<Status>(getStoryObserver));
        BackgroundTaskUtils.runTask(getStoryTask);
    }
}
