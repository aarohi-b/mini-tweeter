package edu.byu.cs.tweeter.server.dao.dynamoDAO;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.server.dao.coolbeans.UserBean;
import edu.byu.cs.tweeter.server.dao.interfaces.IUserDAO;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class UserDAO implements IUserDAO {

    private DynamoDbTable<UserBean> userTable;

    public UserDAO(DynamoDbTable<UserBean> userTable) {
        this.userTable=userTable;
    }
    private static DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
            .region(Region.US_WEST_2)
            .build();
    private static DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    private Key getKey(String userAlias) {
        return Key.builder().partitionValue(userAlias).build();
    }
    private static boolean isNonEmptyString(String value) {
        return (value != null && value.length() > 0);
    }

    public UserBean getUserItem(String username) {
        Key key = getKey(username);
        try {
            return userTable.getItem(key);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get user: "+ e.getMessage());
        }
    }

    public void updateUserItem(String userAlias, Boolean follower, int value) {
        Key key = getKey(userAlias);
        try {
            UserBean item = userTable.getItem(key);
            if(follower){
                item.setFollowerCount(item.getFollowerCount()+value);
            } else {
                item.setFolloweeCount(item.getFolloweeCount()+value);
            }
            userTable.updateItem(item);

        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to update count: "+ e.getMessage());
        }
    }

    @Override
    public Boolean userAlreadyRegistered(String userAlias) {
        Key key = getKey(userAlias);
        return userTable.getItem(key)!= null;
    }

    @Override
    public User login(String username, String passwordHash) throws RuntimeException {
        UserBean userItem = getUserItem(username);
        if (userItem!= null) {
            if(userItem.getPasswordHash().equals(passwordHash)) {
                return new User(userItem.getFirstName(), userItem.getLastName(), userItem.getAlias(), userItem.getImageURL());
            } else throw new RuntimeException("[Bad Request] Wrong password, try again");
        } else throw new RuntimeException("[Bad Request] User does not exist");

    }

    @Override
    public String putS3Image(String username, String image) {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-west-2").build();
        String bucket = "aarohitweeterbucket";
        String fileName = username;
        byte[] imageBytes = Base64.getDecoder().decode(image);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
        ObjectMetadata data = new ObjectMetadata();
        data.setContentLength(imageBytes.length);
        data.setContentType("image/jpeg");

        //file can be read and written to by anyone who has the link to it
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, fileName, byteArrayInputStream, data).withCannedAcl(CannedAccessControlList.PublicRead);
        s3.putObject(putObjectRequest);
        String imageUrl = s3.getUrl(bucket, fileName).toString();

        return imageUrl;
    }


    @Override
    public User getUser(String username) throws RuntimeException {
        UserBean userItem = getUserItem(username);
        return new User(userItem.getFirstName(),userItem.getLastName(),userItem.getAlias(),userItem.getImageURL());
    }

    @Override
    public User register(String username, String password, String firstName, String lastName, String url) throws ParseException {
        UserBean newItem = new UserBean();
        newItem.setFirstName(firstName);
        newItem.setAlias(username);
        newItem.setLastName(lastName);
        newItem.setPasswordHash(password);
        newItem.setImageURL(url);
        newItem.setFolloweeCount(0);
        newItem.setFollowerCount(0);

        try {
            userTable.putItem(newItem);
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to put user in table: "+ e.getMessage());
        }
        return getUser(username);
    }

    @Override
    public void updateFollowerCount(String userAlias, int value) {
        updateUserItem(userAlias, true, value);
    }

    @Override
    public void updateFolloweeCount(String userAlias, int value) {
        updateUserItem(userAlias, false, value);
    }

    @Override
    public Integer getFollowerCount(String userAlias) {
        assert userAlias != null;
        return getUserItem(userAlias).getFollowerCount();
    }

    @Override
    public Integer getFolloweeCount(String userAlias) {
        assert userAlias != null;
        return getUserItem(userAlias).getFolloweeCount();
    }

    @Override
    public void addUserBatch(List<UserBean> users) {
        List<UserBean> batchToWrite = new ArrayList<>();
        for (UserBean u : users) {
            batchToWrite.add(u);

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
    private void writeChunkOfUserDTOs(List<UserBean> userDTOs) {
        if(userDTOs.size() > 25)
            throw new RuntimeException("Too many users to write");

        WriteBatch.Builder<UserBean> writeBuilder = WriteBatch.builder(UserBean.class).mappedTableResource(userTable);
        for (UserBean item : userDTOs) {
            writeBuilder.addPutItem(builder -> builder.item(item));
        }
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBuilder.build()).build();

        try {
            BatchWriteResult result = enhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);

            // just hammer dynamodb again with anything that didn't get written this time
            if (result.unprocessedPutItemsForTable(userTable).size() > 0) {
                writeChunkOfUserDTOs(result.unprocessedPutItemsForTable(userTable));
            }

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

}
