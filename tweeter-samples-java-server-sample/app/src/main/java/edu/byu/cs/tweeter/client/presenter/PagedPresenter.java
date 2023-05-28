package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.GetUserObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class PagedPresenter<T> extends Presenter {
    protected final User user;
    protected final AuthToken authToken;
    private static final String LOG_TAG = "PagedPresenter";

    protected boolean isLoading = false;

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }
    public void getUser(AuthToken authToken, String userAlias){
        userService.getUser(authToken, userAlias, new ConcreteGetUserObserver());
    }

    public boolean isHasMorePages() {
        return hasMorePages;
    }

    protected boolean hasMorePages = true;
    protected UserService userService;
    protected T lastItem;
    public static final int PAGE_SIZE = 10;
    public interface PagedView<Q> extends View {
        void setLoadingStatus(boolean isLoading);
        void addItems(List<Q> u);
        void displayUser(User user);
    }

    public PagedPresenter(PagedView view, User user, AuthToken authToken) {
        super(view);
        this.user = user;
        this.authToken = authToken;
        userService = new UserService();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
    abstract void getItem(AuthToken authToken, User targetUser, int limit, T lastItem);
    abstract T getLastItem();
    abstract void setLastItem(T item);

    public void loadMoreItems() {
        if (!isLoading && hasMorePages) {   // This guard is important for avoiding a race condition in the scrolling code.
            setLoading(true);
            ((PagedView) view).setLoadingStatus(isLoading);

            getItem(authToken, user, PAGE_SIZE, lastItem);
        }
    }

    protected class ConcreteGetItemsObserver extends ConcreteServiceObserver implements PagedObserver<T>{

        @Override
        public void handleSuccess(List<T> items, boolean hasMorePages) {
            setLastItem((items.size() > 0) ? items.get(items.size() - 1) : null);
            setHasMorePages(hasMorePages);
            ((PagedView) view).setLoadingStatus(false);
            ((PagedView) view).addItems(items);
            setLoading(false);

        }
        @Override
        void handleFailureExtra(String message) {
            String errorMessage = "Failed to retrieve item: " + message;
            Log.e(LOG_TAG, errorMessage);
        }

        @Override
        void handleExceptionExtra(Exception ex) {
            String errorMessage = "Failed to retrieve item because of exception: " + ex.getMessage();
            Log.e(LOG_TAG, errorMessage, ex);
        }
    }

    protected class ConcreteGetUserObserver extends ConcreteServiceObserver implements GetUserObserver {

        @Override
        public void handleSuccess(User user) {
            ((PagedView) view).displayUser(user);
        }

        @Override
        void handleFailureExtra(String message) {
            String errorMessage = "Failed to retrieve user: " + message;
            Log.e(LOG_TAG, errorMessage);
        }

        @Override
        void handleExceptionExtra(Exception ex) {
            String errorMessage = "Failed to retrieve user because of exception: " + ex.getMessage();
            Log.e(LOG_TAG, errorMessage, ex);
        }
    }
}
