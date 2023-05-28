package edu.byu.cs.tweeter.client.presenter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.client.model.service.UserService;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.handler.SimpleNotifHandler;
import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.SimpleNotificationObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class MainPresenterUnitTest {
    private MainPresenter.MainView mockView;
    private StatusService mockStatusService;
    private Cache mockCache;
    private User mockUser;
    private AuthToken mockAuthToken;
    private MainPresenter mainPresenterSpy;

    @BeforeEach
    public void setup(){
        //Create mocks
        mockView = Mockito.mock(MainPresenter.MainView.class);
        mockStatusService = Mockito.mock(StatusService.class);
        mockCache = Mockito.mock(Cache.class);
        Cache.setInstance(mockCache);
        mockUser = Mockito.mock(User.class);
        mockAuthToken = Mockito.mock(AuthToken.class);

        mainPresenterSpy = Mockito.spy(new MainPresenter(mockView, mockUser, mockAuthToken));
        Mockito.doReturn(mockStatusService).when(mainPresenterSpy).getStatusService();
    }

    @Test
    public void testPostStatus_successful(){
        String testPost = "Post Status Test";

        Mockito.doAnswer(new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) {
                Status testStatus = invocation.getArgument(1, Status.class);
                MainPresenter.ConcretePostStatusObserver observer = invocation.getArgument(2, MainPresenter.ConcretePostStatusObserver.class);
                observer.handleSuccess();
                Assertions.assertEquals(testStatus.getPost(), testPost);
                return null;
            }
        }).when(mockStatusService).postStatus(Mockito.any(AuthToken.class), Mockito.any(Status.class), Mockito.any(MainPresenter.ConcretePostStatusObserver.class));

        mainPresenterSpy.postStatus(mockAuthToken, testPost);
        Mockito.verify(mockView).postStatusSuccessful();
        Mockito.verify(mockView).displayInfoMessage("Successfully Posted!");

        Mockito.verify(mockStatusService).postStatus(Mockito.any(AuthToken.class), Mockito.any(Status.class), Mockito.any(MainPresenter.ConcretePostStatusObserver.class));
    }

    @Test
    public void testPostStatus_failedWithMessage(){
        String testPost = "Post Status Test";

        Mockito.doAnswer(new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) {
                MainPresenter.ConcretePostStatusObserver observer = invocation.getArgument(2, MainPresenter.ConcretePostStatusObserver.class);
                observer.handleFailure("Test Error");
                return null;
            }
        }).when(mockStatusService).postStatus(Mockito.any(AuthToken.class), Mockito.any(Status.class), Mockito.any(MainPresenter.ConcretePostStatusObserver.class));

        mainPresenterSpy.postStatus(mockAuthToken, testPost);
        Mockito.verify(mockView).displayInfoMessage("Test Error");
    }

    @Test
    public void testPostStatus_failedWithException(){
        String testPost = "Post Status Test";

        Mockito.doAnswer(new Answer<Void>() {
            @Override public Void answer(InvocationOnMock invocation) {
                MainPresenter.ConcretePostStatusObserver observer = invocation.getArgument(2, MainPresenter.ConcretePostStatusObserver.class);
                observer.handleException(new Exception("Test Exception"));
                return null;
            }
        }).when(mockStatusService).postStatus(Mockito.any(AuthToken.class), Mockito.any(Status.class), Mockito.any(MainPresenter.ConcretePostStatusObserver.class));

        mainPresenterSpy.postStatus(mockAuthToken, testPost);
        Mockito.verify(mockView).displayInfoMessage("Test Exception");
    }
}
