package edu.byu.cs.tweeter.server.dao.dynamoDAO;

import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.FollowingRequest;
import edu.byu.cs.tweeter.server.dao.coolbeans.FollowsBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import edu.byu.cs.tweeter.server.dao.interfaces.IFollowDAO;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class FollowDAO implements IFollowDAO {
    private static final String follower_handle_attribute = "followerAlias";
    private static final String followee_handle_attribute = "followeeAlias";

    public static final String IndexName = "switchFollowerFollowee-index";
    private boolean hasMorePagesFollowees = false;
    private boolean hasMorePagesFollowers = false;

    private DynamoDbTable<FollowsBean> followsTable;

    public FollowDAO(DynamoDbTable<FollowsBean> follow) {
        followsTable=follow;
    }

    public void setHasMorePagesFollowees(boolean b){
        this.hasMorePagesFollowees = b;
    }

    public void setHasMorePagesFollowers(boolean b){
        this.hasMorePagesFollowers = b;
    }

    private Key getCompositeKey(String pk_handle, String sk_handle) {
        return Key.builder()
                .partitionValue(pk_handle).sortValue(sk_handle)
                .build();
    }

    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();
    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();


    private Key getPartitionKey(String pk_handle) {
        return Key.builder()
                .partitionValue(pk_handle)
                .build();
    }

    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }

    @Override
    public Pair<List<String>, Boolean> getFollowers(FollowersRequest request) {
        assert request.getLimit() > 0;
        assert request.getFolloweeAlias() != null;

        DynamoDbIndex<FollowsBean> index = followsTable.index(IndexName);

        Key key = getPartitionKey(request.getFolloweeAlias());

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(request.getLimit());

        if(isNonEmptyString(request.getLastFollowerAlias())) {
            // Build up the Exclusive Start Key (telling DynamoDB where you left off reading items)
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(followee_handle_attribute, AttributeValue.builder().s(request.getFolloweeAlias()).build());
            startKey.put(follower_handle_attribute, AttributeValue.builder().s(request.getLastFollowerAlias()).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest query = requestBuilder.scanIndexForward(true).build();
        List<String> followers = new ArrayList<>();
        try {
            SdkIterable<Page<FollowsBean>> sdkIterable = index.query(query);
            PageIterable<FollowsBean> pages = PageIterable.create(sdkIterable);
            pages.stream().limit(1).forEach((Page<FollowsBean> page) -> {
                        setHasMorePagesFollowers(page.lastEvaluatedKey() != null);
                        page.items().forEach(follower_relation -> followers.add(follower_relation.getFollowerAlias()));
                    });

            return new Pair<>(followers, hasMorePagesFollowers);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get followers: getFollowers(), "+ e.getMessage());
        }
    }

    @Override
    public Pair<List<String>, Boolean> getFollowees(FollowingRequest request) {
        assert request.getLimit() > 0;
        assert request.getFollowerAlias() != null;

        Key key = Key.builder().partitionValue(request.getFollowerAlias()).build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).limit(request.getLimit());

        if(isNonEmptyString(request.getLastFolloweeAlias())) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(follower_handle_attribute, AttributeValue.builder().s(request.getFollowerAlias()).build());
            startKey.put(followee_handle_attribute, AttributeValue.builder().s(request.getLastFolloweeAlias()).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest query = requestBuilder.scanIndexForward(true).build();
        List<String> followees = new ArrayList<>();

        try {
            PageIterable<FollowsBean> pages = followsTable.query(query);
            pages.stream().limit(1).forEach((Page<FollowsBean> page) -> {
                        setHasMorePagesFollowees(page.lastEvaluatedKey() != null);
                        page.items().forEach(followee_relation -> followees.add(followee_relation.getFolloweeAlias()));
                    });

            return new Pair<>(followees, hasMorePagesFollowees);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get followees: getFollowees(), "+ e.getMessage());
        }
    }

    @Override
    public void follow(String follower, String followee) {
        FollowsBean newItem = new FollowsBean();
        newItem.setFollowerAlias(follower);
        newItem.setFolloweeAlias(followee);
        try {
            followsTable.putItem(newItem);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to follow: follow(), "+ e.getMessage());
        }
    }

    @Override
    public List<String> getAllFollowers(AuthToken authToken, String followeeAlias) {

        Pair<List<String>, Boolean> response = getFollowers(new FollowersRequest(authToken, followeeAlias,250, null));
        List<String> users = response.getFirst();
        while(response.getSecond()){
            response = getFollowers(new FollowersRequest(authToken, followeeAlias, 250, users.get(users.size()-1)));
            users.addAll(response.getFirst());
        }
        return users;
    }
    @Override
    public void unfollow(String userHandle, String unfollowedUserHandle) {
        Key key = getCompositeKey(userHandle, unfollowedUserHandle);
        try {
            followsTable.deleteItem(key);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to unfollow user: "+ e.getMessage());
        }
    }

    @Override
    public boolean isFollowing(String loggedInUser, String targetUser) {
        Key key = getCompositeKey(loggedInUser, targetUser);
        try {
            return followsTable.getItem(key)!=null;
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to check if following: isFollowing(), "+ e.getMessage());
        }
    }

    @Override
    public void addFollowersBatch(List<String> followers, String followTarget) {
        List<FollowsBean> batchToWrite = new ArrayList<>();

        // Add each user into the TableWriteItems object
        for (String follower : followers) {
            FollowsBean newItem = new FollowsBean();
            newItem.setFollowerAlias(follower);
            newItem.setFolloweeAlias(followTarget);
            batchToWrite.add(newItem);

            if (batchToWrite.size() == 25) {
                // package this batch up and send to DynamoDB.
                writeChunkOfUserDTOs(batchToWrite);
                batchToWrite = new ArrayList<>();
            }
        }

        // write any remaining
        if (batchToWrite.size() > 0) {
            // package this batch up and send to DynamoDB.
            writeChunkOfUserDTOs(batchToWrite);
        }
    }

    private void writeChunkOfUserDTOs(List<FollowsBean> items) {

        if(items.size() > 25)
            throw new RuntimeException("Too many users to write");

        WriteBatch.Builder<FollowsBean> writeBuilder = WriteBatch.builder(FollowsBean.class).mappedTableResource(followsTable);
        for (FollowsBean item : items) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(followsTable).size() > 0) {
                writeChunkOfUserDTOs(result.unprocessedPutItemsForTable(followsTable));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
