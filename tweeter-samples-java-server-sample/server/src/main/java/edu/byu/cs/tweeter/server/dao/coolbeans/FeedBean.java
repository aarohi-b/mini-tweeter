package edu.byu.cs.tweeter.server.dao.coolbeans;

import java.util.List;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class FeedBean {
    private String alias;
    private String authorAlias;
    private String authorfirstName;
    private String authorlastName;
    private String authorimageURL;
    private Long timestamp;
    private String postString;
    private List<String> urls;
    private List<String> mentions;

    @DynamoDbPartitionKey
    public String getAlias() {
        return alias;
    }

    public void setAlias(String userAlias) {
        this.alias = userAlias;
    }
    public String getAuthorAlias() {
        return authorAlias;
    }
    public String getAuthorfirstName() {
        return authorfirstName;
    }

    public void setAuthorfirstName(String authorfirstName) {
        this.authorfirstName = authorfirstName;
    }

    public String getAuthorlastName() {
        return authorlastName;
    }

    public void setAuthorlastName(String authorlastName) {
        this.authorlastName = authorlastName;
    }

    public String getAuthorimageURL() {
        return authorimageURL;
    }

    public void setAuthorimageURL(String authorimageURL) {
        this.authorimageURL = authorimageURL;
    }


    public void setAuthorAlias(String authorAlias) {
        this.authorAlias = authorAlias;
    }

    public String getPostString() {
        return postString;
    }

    public void setPostString(String postString) {
        this.postString = postString;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
    @DynamoDbSortKey
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    public List<String> getMentions() {
        return mentions;
    }

    public void setMentions(List<String> mentions) {
        this.mentions = mentions;
    }
}
