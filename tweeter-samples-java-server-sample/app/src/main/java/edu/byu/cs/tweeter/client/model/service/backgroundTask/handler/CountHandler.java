package edu.byu.cs.tweeter.client.model.service.backgroundTask.handler;

import android.os.Bundle;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.GetFollowersCountTask;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.CountObserver;

public class CountHandler extends BackgroundTaskHandler<CountObserver>{
    boolean isFollowersCount;
    public CountHandler(CountObserver observer, boolean isFollowersCount) {
        super(observer);
        this.isFollowersCount=isFollowersCount;
    }

    @Override
    protected void handleSuccess(Bundle data, CountObserver observer) {
        int count = data.getInt(GetFollowersCountTask.COUNT_KEY);
        observer.handleSuccess(count, isFollowersCount);
    }
}
