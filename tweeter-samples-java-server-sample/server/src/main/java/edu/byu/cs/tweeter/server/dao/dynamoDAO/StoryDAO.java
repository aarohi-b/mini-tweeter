package edu.byu.cs.tweeter.server.dao.dynamoDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.StoryBean;
import edu.byu.cs.tweeter.server.dao.interfaces.IStoryDAO;
import edu.byu.cs.tweeter.util.Pair;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class StoryDAO implements IStoryDAO {
    private static final String PARTITION_KEY = "authorAlias";
    private static final String SORT_KEY = "timestamp";

    public DynamoDbTable<StoryBean> getStoryTable() {
        return storyTable;
    }

    public void setStoryTable(DynamoDbTable<StoryBean> storyTable) {
        this.storyTable = storyTable;
    }

    private boolean hasMorePages = false;

    private DynamoDbTable<StoryBean> storyTable;
    public StoryDAO(DynamoDbTable<StoryBean> story) {
        storyTable=story;
    }

    public boolean isHasMorePages() {
        return hasMorePages;
    }

    public void setHasMorePages(boolean hasMorePages) {
        this.hasMorePages = hasMorePages;
    }
    @Override
    public Pair<List<Status>, Boolean> getStory(User user, Status lastStatus, int limit) {
        Key key = Key.builder().partitionValue(user.getAlias()).build();

        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(key)).limit(limit);

        if(lastStatus!=null) {
            Map<String, AttributeValue> startKey = new HashMap<>();
            startKey.put(PARTITION_KEY, AttributeValue.builder().s(user.getAlias()).build());
            startKey.put(SORT_KEY, AttributeValue.builder().n(String.valueOf(lastStatus.getTimestamp())).build());

            requestBuilder.exclusiveStartKey(startKey);
        }
        QueryEnhancedRequest request = requestBuilder.scanIndexForward(false).build();

        List<Status> statuses = new ArrayList<>();
        try {
            PageIterable<StoryBean> pages = storyTable.query(request);
            pages.stream().limit(1).forEach((Page<StoryBean> page) -> {
                setHasMorePages(page.lastEvaluatedKey() != null);
                page.items().forEach(status -> statuses.add(new Status(status.getPostString(), user, status.getTimestamp(), status.getUrls(), status.getMentions())));
            });
            return new Pair<>(statuses, hasMorePages);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get story: getStory()" + e.getMessage());
        }
    }

    @Override
    public boolean postStatus(Status status) {
        try {
            StoryBean newItem = new StoryBean();
            newItem.setTimestamp(status.getTimestamp());
            newItem.setPostString(status.getPost());
            newItem.setMentions(status.getMentions());
            newItem.setUrls(status.getUrls());
            newItem.setAuthorAlias(status.getUser().getAlias());
            storyTable.putItem(newItem);
            return true;
        }catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to post status to story: postStatus(), "+ e.getMessage());
        }
    }
}
