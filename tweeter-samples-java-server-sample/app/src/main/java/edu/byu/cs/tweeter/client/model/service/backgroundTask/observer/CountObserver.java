package edu.byu.cs.tweeter.client.model.service.backgroundTask.observer;

public interface CountObserver extends ServiceObserver{
    void handleSuccess(int count, boolean isCountFollowers);
}
