package edu.byu.cs.tweeter.server.dao.coolbeans;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class FollowsBean {
    private String followerAlias;
    private String followeeAlias;
    public static final String IndexName = "switchFollowerFollowee-index";


    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = IndexName)
    public String getFollowerAlias() {
        return followerAlias;
    }

    public void setFollowerAlias(String follower_handle) {
        this.followerAlias = follower_handle;
    }

    @DynamoDbSortKey
    @DynamoDbSecondaryPartitionKey(indexNames = IndexName)
    public String getFolloweeAlias() {
        return followeeAlias;
    }

    public void setFolloweeAlias(String followee_handle) {
        this.followeeAlias = followee_handle;
    }

    @Override
    public String toString() {
        return "Follows{" +
                "follower_handle='" + followerAlias + '\'' +
                ", followee_handle=" + followeeAlias + '\'' +
                '}';
    }
}
