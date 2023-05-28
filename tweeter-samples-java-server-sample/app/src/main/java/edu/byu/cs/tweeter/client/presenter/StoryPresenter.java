package edu.byu.cs.tweeter.client.presenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter extends PagedPresenter<Status> {
    @Override
    void getItem(AuthToken authToken, User targetUser, int limit, Status lastItem) {
        userService.getStory(authToken, targetUser, limit, lastItem, new ConcreteGetItemsObserver());
    }

    @Override
    Status getLastItem() {
        return lastItem;
    }

    @Override
    void setLastItem(Status item) {
        lastItem = item;
    }

    public StoryPresenter(PagedView view, User user, AuthToken authToken) {
        super(view, user, authToken);
    }
}
