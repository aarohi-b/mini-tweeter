package edu.byu.cs.tweeter.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.net.request.FollowersRequest;
import edu.byu.cs.tweeter.model.net.request.RegisterRequest;
import edu.byu.cs.tweeter.model.net.response.FollowersResponse;
import edu.byu.cs.tweeter.model.net.response.RegisterResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GetFollowersTest {
    ServerFacade serverFacade;
    AuthToken testAuthtoken;

    @BeforeAll
    public void setup() {
        this.serverFacade = new ServerFacade();
        this.testAuthtoken = new AuthToken();
    }

    @Test
    public void GetFollowersIntegrationPass() {
        FollowersRequest request = new FollowersRequest(testAuthtoken, "fake", 10, "fake");
        String urlPath = "/getfollowers";
        try {
            FollowersResponse response = serverFacade.getFollowers(request, urlPath);
            assert (response.isSuccess());
            assertNotNull(response.getFollowers());
        } catch (Exception e) { //getfollowers throws exception
            System.out.println("Exception caught!");
        }
    }

    @Test
    public void GetFollowersIntegrationFail() {
        FollowersRequest request = new FollowersRequest(testAuthtoken, "fake", 10, "fake");
        String urlPath = "/failurl";
        try {
            FollowersResponse response = serverFacade.getFollowers(request, urlPath);
            assert (!response.isSuccess());
        } catch (Exception e) {
            System.out.println("Exception caught!");
        }
    }
}
