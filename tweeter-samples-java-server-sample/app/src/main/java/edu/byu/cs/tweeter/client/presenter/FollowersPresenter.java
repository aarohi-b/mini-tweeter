package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.FollowService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
public class FollowersPresenter extends PagedPresenter<User>{
    /**
     * Creates an instance.
     *
     * @param view      the view for which this class is the presenter.
     * @param user      the user that is currently logged in.
     * @param authToken the auth token for the current session.
     */
    public FollowersPresenter(PagedView view, User user, AuthToken authToken) {
        super(view,user,authToken);
    }
    @Override
    void getItem(AuthToken authToken, User targetUser, int limit, User lastItem) {
        new FollowService().getFollowers(authToken, targetUser, limit, lastItem, new ConcreteGetItemsObserver());
    }

    @Override
    User getLastItem() {
        return lastItem;
    }

    @Override
    void setLastItem(User item) {
        lastItem = item;
    }

}
