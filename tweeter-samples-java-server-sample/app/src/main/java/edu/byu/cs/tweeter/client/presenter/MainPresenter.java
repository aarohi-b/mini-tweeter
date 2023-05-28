package edu.byu.cs.tweeter.client.presenter;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.CountObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.IsFollowerObserver;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenter extends Presenter{
    private static final String LOG_TAG = "MainActivity";
    private final User user;
    private final AuthToken authToken;
    private UserService userService;
    private StatusService statusService;

    public MainPresenter(MainView view, User user, AuthToken authToken) {
        super(view);
        this.user = user;
        this.authToken = authToken;
    }

    public interface MainView extends View{
        void isFollower(boolean isFollower);
        void setCount(int count, boolean isFollowerCount);
        void enableFollowButton();
        void followSuccessful();
        void unfollowSuccessful();
        void logoutSuccessful();
        void postStatusSuccessful();

    }

    protected UserService getUserService(){
        if (userService == null){
            userService = new UserService();
        }
        return userService;
    }

    protected StatusService getStatusService(){
        if (statusService == null){
            statusService = new StatusService();
        }
        return statusService;
    }
    public void getFollowersCount(AuthToken authToken, User user){
        new FollowService().getFollowersCount(authToken, user, new ConcreteGetCountObserver());
    }

    public void getFollowingCount(AuthToken authToken, User user){
        new FollowService().getFollowingCount(authToken, user, new ConcreteGetCountObserver());
    }
    public void findIfFollower(AuthToken authToken, User user, User followee){
        new FollowService().findIfFollower(authToken, user, followee, new ConcreteIsFollowerObserver());
    }

    public void follow(AuthToken authToken, User followee){
        new FollowService().follow(authToken, user, followee, new ConcreteFollowObserver());
    }

    public void unfollow(AuthToken authToken, User myUser, User followee){
        new FollowService().unfollow(authToken, myUser, followee, new ConcreteUnfollowObserver());
    }

    public void logout(AuthToken authToken){
        getUserService().logout(authToken, new ConcreteLogoutObserver());
    }
    public void postStatus(AuthToken authToken, String post){
        try {
            Status newStatus = new Status(post, Cache.getInstance().getCurrUser(), System.currentTimeMillis(), parseURLs(post), parseMentions(post));
            getStatusService().postStatus(authToken, newStatus, new ConcretePostStatusObserver());

        } catch (Exception ex) {
            //Log.e(LOG_TAG, ex.getMessage(), ex);
            view.displayInfoMessage("Failed to post the status because of exception: " + ex.getMessage());
        }
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }
        return containedUrls;
    }
    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }
    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }


    }
    protected class ConcretePostStatusObserver extends ConcreteServiceObserver implements SimpleNotificationObserver{

        @Override
        public void handleSuccess() {
            ((MainView) view).postStatusSuccessful();
            view.displayInfoMessage("Successfully Posted!");
        }
        @Override
        void handleFailureExtra(String message) {

        }

        @Override
        void handleExceptionExtra(Exception ex) {

        }
    }

    private class ConcreteLogoutObserver extends ConcreteServiceObserver implements SimpleNotificationObserver {
        @Override
        public void handleSuccess() {
            ((MainView) view).logoutSuccessful();
        }
        @Override
        void handleFailureExtra(String message) {
        }
        @Override
        void handleExceptionExtra(Exception ex) {

        }
    }

    private class ConcreteGetCountObserver extends ConcreteServiceObserver implements CountObserver{
        @Override
        public void handleSuccess(int count, boolean isCountFollowers) {
            ((MainView) view).setCount(count, isCountFollowers);
        }

        @Override
        void handleFailureExtra(String message) {

        }

        @Override
        void handleExceptionExtra(Exception ex) {

        }
    }

    private class ConcreteFollowObserver extends ConcreteServiceObserver implements SimpleNotificationObserver{

        @Override
        public void handleSuccess() {
            ((MainView) view).followSuccessful();
            ((MainView) view).enableFollowButton();
        }
        @Override
        void handleFailureExtra(String message) {
            ((MainView) view).enableFollowButton();
        }

        @Override
        void handleExceptionExtra(Exception ex) {
            ((MainView) view).enableFollowButton();
        }
    }

    private class ConcreteUnfollowObserver extends ConcreteServiceObserver implements SimpleNotificationObserver{

        @Override
        public void handleSuccess() {
            ((MainView) view).unfollowSuccessful();
            ((MainView) view).enableFollowButton();
        }

        @Override
        void handleFailureExtra(String message) {
            ((MainView) view).enableFollowButton();
        }

        @Override
        void handleExceptionExtra(Exception ex) {
            ((MainView) view).enableFollowButton();
        }
    }
    private class ConcreteIsFollowerObserver extends ConcreteServiceObserver implements IsFollowerObserver {

        @Override
        public void handleSuccess(boolean isFollower) {
            ((MainView) view).isFollower(isFollower);
        }

        @Override
        void handleFailureExtra(String message) {

        }

        @Override
        void handleExceptionExtra(Exception ex) {

        }
    }
}
