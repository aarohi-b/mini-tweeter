import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;
import edu.byu.cs.tweeter.model.net.request.StoryRequest;
import edu.byu.cs.tweeter.model.net.response.LoginResponse;
import edu.byu.cs.tweeter.model.net.response.StoryResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StoryPostingTest {
    private User user;
    private AuthToken authToken;
    private Status testStatus;
    ServerFacade serverFacade;
    private MainPresenter.MainView mockMainView;
    private MainPresenter mainPresenterSpy;
    private CountDownLatch countDownLatch;

    private void resetCountdownLatch() {
        countDownLatch = new CountDownLatch(1);
    }

    private void awaitCountdownLatch() throws InterruptedException {
        countDownLatch.await();
        resetCountdownLatch();
    }


    @BeforeEach
    public void setup() {
        this.serverFacade = new ServerFacade();

        LoginRequest request = new LoginRequest("@banana", "pass");
        String urlPath = "/login";
        try {
            LoginResponse response = serverFacade.login(request, urlPath);
            assert(response.isSuccess());
            assertNotNull(response.getUser());
            authToken = response.getAuthToken();
            user=response.getUser();
            Cache.getInstance().setCurrUser(user);
            Cache.getInstance().setCurrUserAuthToken(authToken);

        } catch (Exception e) { //register throws exception
            System.out.println(e.getMessage());
        }

        //spy the main presenter
        mockMainView = Mockito.mock(MainPresenter.MainView.class);
        mainPresenterSpy = Mockito.spy(new MainPresenter(mockMainView, user, authToken));

        //make a test status
        String mention = "@testMention";
        List<String> mentions = Collections.singletonList(mention);
        String url = "https://byu.edu";
        List<String> urls = Collections.singletonList(url);
        String postStr = "Post Test:" +
                "\nMy friend " + mention + " likes this website" + "\n" + url;
        Long timestamp = System.currentTimeMillis();

        testStatus = new Status(postStr, user, timestamp, urls, mentions);

        resetCountdownLatch();
    }

    @Test
    public void testPostToStory() throws Exception {
        Answer<Void> postStatusSuccess = new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                Assertions.assertEquals("Successfully Posted!", invocation.getArgument(0));
                countDownLatch.countDown();
                return null;
            }
        };

        Mockito.doAnswer(postStatusSuccess).when(mockMainView).displayInfoMessage(Mockito.any());
        mainPresenterSpy.postStatus(authToken, testStatus.getPost());

        awaitCountdownLatch();
        StoryRequest request = new StoryRequest(authToken, "@banana", 10, null);
        String urlPath = "/getstory";
        try {
            StoryResponse response = serverFacade.getStory(request, urlPath);
            assert (response.isSuccess());
            List<Status> story = response.getStory();
            Status uploadedStatus = story.get(0);
            assertEquals(uploadedStatus, testStatus);

        } catch (Exception e) {
            System.out.println("Exception caught!");
        }
    }

}