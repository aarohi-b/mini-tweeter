package edu.byu.cs.tweeter.server.dao.factory;

import java.util.ArrayList;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import edu.byu.cs.tweeter.server.service.Service;

public class Filler {

    private static final IDAOAbstractFactory factory = new DynamoFactory();

    // How many follower users to add
    // We recommend you test this with a smaller number first, to make sure it works for you
    private final static int NUM_USERS = 10000;

    // The alias of the user to be followed by each user created
    // This example code does not add the target user, that user must be added separately.
    private final static String FOLLOW_TARGET = "@testUser";

    public static void fillDatabase() {
        List<String> followers = new ArrayList<>();
        List<UserBean> users = new ArrayList<>();

        String imageURL="https://aarohitweeterbucket.s3.us-west-2.amazonaws.com/JaketheDog.jpg";

        UserBean testUser = new UserBean();
        testUser.setAlias(FOLLOW_TARGET);
        testUser.setFirstName("Aarohi");
        testUser.setLastName("Bhatt");
        testUser.setImageURL(imageURL);
        testUser.setFolloweeCount(0);
        testUser.setFollowerCount(NUM_USERS);
        testUser.setPasswordHash(Service.getSecurePassword("pass"));
        users.add(testUser);

        // Iterate over the number of users you will create
        for (int i = 1; i <= NUM_USERS; i++) {

            String name = "Guy " + i;
            String alias = "guy" + i;

            // Note that in this example, a UserDTO only has a name and an alias.
            // The url for the profile image can be derived from the alias in this example
            UserBean newItem = new UserBean();
            newItem.setAlias(alias);
            newItem.setFirstName(name);
            newItem.setLastName("Last");
            newItem.setImageURL(imageURL);
            newItem.setFolloweeCount(1);
            newItem.setFollowerCount(0);
            newItem.setPasswordHash(Service.getSecurePassword("pass"));
            users.add(newItem);

            // Note that in this example, to represent a follows relationship, only the aliases
            // of the two users are needed
            followers.add(alias);
        }

        // Call the DAOs for the database logic
        if (users.size() > 0) {
            factory.getUserDAO().addUserBatch(users);
        }
        if (followers.size() > 0) {
            factory.getFollowDAO().addFollowersBatch(followers, FOLLOW_TARGET);
        }
    }

    public static void main(String[] args){
        fillDatabase();
    }
}