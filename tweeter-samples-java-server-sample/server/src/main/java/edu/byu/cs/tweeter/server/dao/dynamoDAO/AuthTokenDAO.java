package edu.byu.cs.tweeter.server.dao.dynamoDAO;
import edu.byu.cs.tweeter.server.dao.coolbeans.AuthTokenBean;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.server.dao.interfaces.IAuthTokenDAO;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

public class AuthTokenDAO implements IAuthTokenDAO {
    private static final SecureRandom secureRandom = new SecureRandom(); //faster to have 1 instance
    private DynamoDbTable<AuthTokenBean> authTable;
    public AuthTokenDAO(DynamoDbTable<AuthTokenBean> authToken) {
        authTable=authToken;
    }

    public AuthToken generateToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String auth = Base64.getUrlEncoder().encodeToString(randomBytes);
        String currTimeString = String.valueOf((new Timestamp(System.currentTimeMillis())).getTime());
        AuthToken authToken = new AuthToken(auth, currTimeString);

        try {
            AuthTokenBean newItem = new AuthTokenBean();
            newItem.setAuthToken(authToken.getToken());
            newItem.setTimestamp(Long.valueOf(currTimeString));
            authTable.putItem(newItem);
        }
        catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to put authToken: generateToken(), "+ e.getMessage());
        }

        return authToken;
    }

    public boolean deleteToken(AuthToken authToken) {
        try {
            AuthTokenBean newItem = new AuthTokenBean();
            newItem.setAuthToken(authToken.getToken());
            authTable.deleteItem(newItem);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to delete authToken: deleteToken()"+ e.getMessage());
        }

    }

    public AuthToken getToken(String token) {
        try {
            Key key = Key.builder().partitionValue(token).build();
            AuthTokenBean item = authTable.getItem(key);
            return new AuthToken(item.getAuthToken(), item.getTimestamp().toString());
        }catch (Exception e) {
            throw new RuntimeException("[Internal Error] Failed to get authToken: getToken(), "+ e.getMessage());
        }

    }

    @Override
    public AuthToken validateAuthToken(AuthToken authToken) {
        Key key = Key.builder().partitionValue(authToken.token).build();
        AuthTokenBean item = authTable.getItem(key);
        Long retrievedAuthTimestamp = item.getTimestamp();
        Long currTime = System.currentTimeMillis();
        long timeDiff = (currTime - retrievedAuthTimestamp) / 60000; //get time diff in minutes
        if (timeDiff >= 15) {
            authTable.deleteItem(key);
        } else {
            item.setTimestamp(currTime);
            try {
                AuthTokenBean updatedItem = authTable.updateItem(item);
                System.out.println("validateAuthToken successfully");
            } catch (Exception e) {
                System.err.println("validateAuthToken failed: " + authToken.getToken());
                System.err.println(e.getMessage());
            }
        }
        return new AuthToken(authToken.getToken(), currTime.toString());
    }

}