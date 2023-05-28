package edu.byu.cs.tweeter.client;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.request.FollowingCountRequest;
import edu.byu.cs.tweeter.model.net.response.FollowingCountResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetFollowingCountTest {
    ServerFacade serverFacade;
    AuthToken testAuthtoken;

    @BeforeAll
    public void setup() {
        this.serverFacade = new ServerFacade();
        this.testAuthtoken = new AuthToken();
    }

    @Test
    public void RegisterIntegrationPass() {
        FollowingCountRequest request = new FollowingCountRequest("testUser", testAuthtoken);
        String urlPath = "/getfollowingcount";
        try {
            FollowingCountResponse response = serverFacade.getFollowingCount(request, urlPath);
            assert (response.isSuccess());
            Assertions.assertNotEquals(0, response.getFollowingCount());
        } catch (Exception e) {
            System.out.println("Exception caught!");
        }
    }

    @Test
    public void RegisterIntegrationFail() {
        FollowingCountRequest request = new FollowingCountRequest("testUser", testAuthtoken);
        String urlPath = "/failurl";
        try {
            FollowingCountResponse response = serverFacade.getFollowingCount(request, urlPath);
            assert (!response.isSuccess());
        } catch (Exception e) {
            System.out.println("Exception caught!");
        }
    }
}
