package edu.byu.cs.tweeter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.PagedObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.FakeData;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {
    private User currentUser;
    private AuthToken currentAuthToken;
    private FakeObserver observer;
    private FakeObserver observerSpy;
    private UserService userServiceSpy;
    private CountDownLatch countDownLatch;

    private class FakeObserver implements PagedObserver<Status>{
        private boolean success;
        private String message;
        private List<Status> items;
        private boolean hasMorePages;
        private Exception exception;

        @Override
        public void handleSuccess(List<Status> items, boolean hasMorePages) {
            this.success = true;
            this.message = null;
            this.items = items;
            this.hasMorePages = hasMorePages;
            this.exception = null;
            countDownLatch.countDown();
        }
        @Override
        public void handleFailure(String msg) {

        }

        @Override
        public void handleException(Exception ex) {

        }
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public List<Status> getItems() {
            return items;
        }

        public boolean getHasMorePages() {
            return hasMorePages;
        }

        public Exception getException() {
            return exception;
        }
    }

    private void resetCountdownLatch() {
        countDownLatch = new CountDownLatch(1);
    }

    private void awaitCountdownLatch() throws InterruptedException {
        countDownLatch.await();
        resetCountdownLatch();
    }

    @BeforeAll
    public void setup() {
        currentUser = new User("FirstName", "LastName", null);
        currentAuthToken = new AuthToken();
        userServiceSpy = Mockito.spy(new UserService());

        // Setup an observer for the FollowService
        observer = new FakeObserver();
        // Prepare the countdown latch
        resetCountdownLatch();
    }

    @Test
    public void testGetStory_pass() throws InterruptedException {
        userServiceSpy.getStory(currentAuthToken, currentUser, 3, null, observer);
        awaitCountdownLatch();
        List<Status> expectedStatus = FakeData.getInstance().getFakeStatuses().subList(0, 3);
        Assertions.assertTrue(observer.isSuccess());
        Assertions.assertNull(observer.getMessage());
        List<Status> actualStatuses = observer.getItems();
        Assertions.assertEquals(expectedStatus.size(), actualStatuses.size());
        Assertions.assertEquals(expectedStatus, actualStatuses);
        Assertions.assertTrue(observer.getHasMorePages());
        Assertions.assertNull(observer.getException());
    }
}
