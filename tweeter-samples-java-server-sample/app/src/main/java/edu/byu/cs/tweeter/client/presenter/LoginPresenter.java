package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.UserService;

public class LoginPresenter extends AuthenticatePresenter {
    public LoginPresenter(AuthenticateView view) {
        super(view);
    }

    public void initiateLogin(String username, String password){
        String validationMsg = validateLogin(username, password);

        if (validationMsg ==null){
            view.displayInfoMessage("Logging in...");
            UserService service = new UserService();
            service.login(username,password, new ConcreteAuthenticateObserver());
        } else {
            ((AuthenticateView)view).displayErrorMessage(validationMsg);
        }
    }

    public String validateLogin(String username, String pass) {
        if (username.length() > 0 && username.charAt(0) != '@') {
            return "Alias must begin with @.";
        }
        if (username.length() < 2) {
            return "Alias must contain 1 or more characters after the @.";
        }
        if (pass.length() == 0) {
            return "Password cannot be empty.";
        }
        return null;
    }

}
