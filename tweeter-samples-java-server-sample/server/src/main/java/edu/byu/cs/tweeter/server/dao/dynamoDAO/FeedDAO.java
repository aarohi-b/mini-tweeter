package edu.byu.cs.tweeter.server.dao.dynamoDAO;
import edu.byu.cs.tweeter.server.dao.coolbeans.FollowsBean;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.FeedBean;
import edu.byu.cs.tweeter.server.dao.interfaces.IFeedDAO;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class FeedDAO implements IFeedDAO {
    private static final String PARTITION_KEY = "alias";
    private static final String SORT_KEY = "timestamp";
    private boolean hasMorePages = false;
    private DynamoDbTable<FeedBean> feedTable;
    public FeedDAO(DynamoDbTable<FeedBean> feedTable) {
        this.feedTable = feedTable;
    }
    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }

    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();
    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();

    @Override
    public Pair<List<Status>, Boolean> getFeed(String username, Status lastStatus, Integer limit) {
        Key key = Key.builder().partitionValue(username).build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key))
                .limit(limit);

        if(lastStatus!=null) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(PARTITION_KEY, AttributeValue.builder().s(username).build());
            startKey.put(SORT_KEY, AttributeValue.builder().n(String.valueOf(lastStatus.getTimestamp())).build());

            requestBuilder.exclusiveStartKey(startKey);
        }

        QueryEnhancedRequest request = requestBuilder.scanIndexForward(false).build();
        List<Status> statuses = new ArrayList<>();

        try {
            PageIterable<FeedBean> pages = feedTable.query(request);

            pages.stream().limit(1).forEach((Page<FeedBean> page) -> {
                setHasMorePages(page.lastEvaluatedKey() != null);
                page.items().forEach(status -> statuses.add(new Status(status.getPostString(),
                        new User(status.getAuthorfirstName(), status.getAuthorlastName(), status.getAuthorAlias(), status.getAuthorimageURL()),
                        status.getTimestamp(), status.getUrls(), status.getMentions())));
            });

            return new Pair<>(statuses, hasMorePages);
        }catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get feed: getFeed()"+ e.getMessage());
        }
    }

    @Override
    public boolean postStatus(Status status, List<String> followers) {
        User statusPoster  = status.getUser();
        try {
            for (String follower : followers) {

                FeedBean newItem = new FeedBean();
                newItem.setTimestamp(status.getTimestamp());
                newItem.setPostString(status.getPost());
                newItem.setMentions(status.getMentions());
                newItem.setUrls(status.getUrls());
                newItem.setAlias(follower);
                newItem.setAuthorAlias(statusPoster.getAlias());
                newItem.setAuthorfirstName(statusPoster.getFirstName());
                newItem.setAuthorlastName(statusPoster.getLastName());
                newItem.setAuthorimageURL(statusPoster.getImageUrl());

                feedTable.putItem(newItem);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to post status to feed: postStatus(), "+ e.getMessage());
        }
        return true;
    }

    @Override
    public void updateFeedBatch(Status status, List<String> followers) {
        List<FeedBean> batchToWrite = new ArrayList<>();

        // Add each user into the TableWriteItems object
        for (String follower : followers) {
            User author = status.getUser();
            FeedBean newItem = new FeedBean();
            newItem.setAlias(follower);
            newItem.setAuthorimageURL(author.getImageUrl());
            newItem.setAuthorlastName(author.getLastName());
            newItem.setAuthorfirstName(author.getFirstName());
            newItem.setPostString(status.getPost());
            newItem.setTimestamp(status.getTimestamp());
            newItem.setAuthorAlias(author.getAlias());
            newItem.setUrls(status.getUrls());
            newItem.setMentions(status.getMentions());
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

    private void writeChunkOfUserDTOs(List<FeedBean> items) {

        if(items.size() > 25)
            throw new RuntimeException("Too many users to write");

        WriteBatch.Builder<FeedBean> writeBuilder = WriteBatch.builder(FeedBean.class).mappedTableResource(feedTable);
        for (FeedBean item : items) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(feedTable).size() > 0) {
                writeChunkOfUserDTOs(result.unprocessedPutItemsForTable(feedTable));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
