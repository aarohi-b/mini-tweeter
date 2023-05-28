package edu.byu.cs.tweeter.server.dao.factory;

import edu.byu.cs.tweeter.server.dao.interfaces.*;

public interface IDAOAbstractFactory {
    public abstract IUserDAO getUserDAO();
    public abstract IFollowDAO getFollowDAO();
    public abstract IAuthTokenDAO getAuthTokenDAO();
    public abstract IFeedDAO getFeedDAO();
    public abstract IStoryDAO getStoryDAO();
}
