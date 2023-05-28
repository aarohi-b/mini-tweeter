package edu.byu.cs.tweeter.server.dao.interfaces;

import java.text.ParseException;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;

public interface IUserDAO {
    Boolean userAlreadyRegistered(String userAlias);

    User login(String username, String password) throws RuntimeException;

    String putS3Image(String username, String image);

    User getUser(String username) throws RuntimeException;
//    public boolean logout() throws ParseException;
    User register(String username, String password, String firstName, String lastName, String url) throws ParseException;

    void updateFollowerCount(String userAlias, int value);

    void updateFolloweeCount(String userAlias, int value);

    Integer getFollowerCount(String userAlias);

    Integer getFolloweeCount(String userAlias);

    void addUserBatch(List<UserBean> users);
}
