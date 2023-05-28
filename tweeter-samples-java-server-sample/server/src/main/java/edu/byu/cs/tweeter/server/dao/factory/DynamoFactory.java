package edu.byu.cs.tweeter.server.dao.factory;

import edu.byu.cs.tweeter.server.dao.coolbeans.AuthTokenBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.FeedBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.FollowsBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.StoryBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import edu.byu.cs.tweeter.server.dao.dynamoDAO.*;
import edu.byu.cs.tweeter.server.dao.interfaces.*;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoFactory implements IDAOAbstractFactory{
    private IUserDAO userDAO;
    private IAuthTokenDAO authTokenDAO;
    private IFeedDAO feedDAO;
    private IStoryDAO storyDAO;
    private IFollowDAO followDAO;
    private static DynamoDbEnhancedClient enhancedClient;

    public DynamoDbEnhancedClient getDynamoClient(){
        if(enhancedClient == null){
            DynamoDbClient amazonDynamoDB = DynamoDbClient.builder().region(Region.US_WEST_2).build();
            enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(amazonDynamoDB)
                    .build();
        }
        return enhancedClient;
    }

    @Override
    public IUserDAO getUserDAO() {
        if(userDAO == null){
            DynamoDbTable<UserBean> userTable = getDynamoClient().table("User", TableSchema.fromBean(UserBean.class));
            userDAO=new UserDAO(userTable);
        }

        return userDAO;
    }

    @Override
    public IFollowDAO getFollowDAO() {
        if(followDAO == null){
            DynamoDbTable<FollowsBean> followTable = getDynamoClient().table("Follows", TableSchema.fromBean(FollowsBean.class));
            followDAO=new FollowDAO(followTable);
        }
        return followDAO;
    }

    @Override
    public IAuthTokenDAO getAuthTokenDAO() {
        if(authTokenDAO==null){
            DynamoDbTable<AuthTokenBean> authTokenTable = getDynamoClient().table("AuthToken", TableSchema.fromBean(AuthTokenBean.class));
            authTokenDAO=new AuthTokenDAO(authTokenTable);
        }
        return authTokenDAO;
    }

    @Override
    public IFeedDAO getFeedDAO() {
        if(feedDAO == null){
            DynamoDbTable<FeedBean> feedTable = getDynamoClient().table("Feed", TableSchema.fromBean(FeedBean.class));
            feedDAO = new FeedDAO(feedTable);
        }
        return feedDAO;
    }

    @Override
    public IStoryDAO getStoryDAO() {
        if(storyDAO == null){
            DynamoDbTable<StoryBean> storytable = getDynamoClient().table("Story", TableSchema.fromBean(StoryBean.class));
            storyDAO = new StoryDAO(storytable);
        }
        return storyDAO;
    }
}
