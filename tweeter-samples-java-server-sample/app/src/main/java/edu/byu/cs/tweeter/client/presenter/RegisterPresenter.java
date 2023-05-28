package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.model.service.UserService;

public class RegisterPresenter extends AuthenticatePresenter {
    public RegisterPresenter(AuthenticateView view) {
        super(view);
    }

    public void initiateRegister(String firstName, String lastName, String alias, String password, String imageBytesBase64) {
        String validationMsg = validateRegistration(firstName, lastName, alias, password);
        if (validationMsg ==null){
            view.displayInfoMessage("Registering...");
            UserService service = new UserService();

            service.register(firstName, lastName, alias, password, imageBytesBase64, new ConcreteAuthenticateObserver());

        } else {
            ((AuthenticateView)view).displayErrorMessage(validationMsg);
        }
    }

    public String validateRegistration(String firstName, String lastName, String alias, String pass) {
        if (firstName.length() == 0) {
            return "First Name cannot be empty.";
        }
        if (lastName.length() == 0) {
            return ("Last Name cannot be empty.");
        }
        if (alias.length() == 0) {
            return ("Alias cannot be empty.");
        }
        if (alias.charAt(0) != '@') {
            return ("Alias must begin with @.");
        }
        if (alias.length() < 2) {
            return ("Alias must contain 1 or more characters after the @.");
        }
        if (pass.length() == 0) {
            return ("Password cannot be empty.");
        }
        return null;
    }
}
