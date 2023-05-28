package edu.byu.cs.tweeter.client.model.service;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.BackgroundTaskUtils;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.PostStatusTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.SimpleNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;

public class StatusService {
    public void postStatus(AuthToken authToken, Status status, SimpleNotificationObserver observer){
        PostStatusTask postStatusTask = new PostStatusTask(authToken, status, new SimpleNotifHandler(observer));
        BackgroundTaskUtils.runTask(postStatusTask);
    }

}
