package edu.byu.cs.tweeter.client.model.service.backgroundTask.observer;

public interface ServiceObserver{
    void handleFailure(String msg);
    void handleException(Exception ex);
}
