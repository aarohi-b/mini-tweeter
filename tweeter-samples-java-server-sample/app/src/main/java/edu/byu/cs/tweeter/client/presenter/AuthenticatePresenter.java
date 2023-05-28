package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.backgroundTask.observer.AuthObserver;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public abstract class AuthenticatePresenter extends Presenter{
    public AuthenticatePresenter(AuthenticateView view) {
        super(view);
    }

    public interface AuthenticateView extends View {
        public void displayErrorMessage(String message);

        public void actionSuccessful(User user, AuthToken authToken);
    }

    protected class ConcreteAuthenticateObserver extends ConcreteServiceObserver implements AuthObserver {
        @Override
        public void handleSuccess(User authenticateUser, AuthToken authToken) {
            ((AuthenticateView) view).actionSuccessful(authenticateUser, authToken);
        }
        @Override
        void handleFailureExtra(String message) {

        }

        @Override
        void handleExceptionExtra(Exception ex) {

        }
    }
}
