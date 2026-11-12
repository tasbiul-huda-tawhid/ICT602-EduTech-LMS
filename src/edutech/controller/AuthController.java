package edutech.controller;

import edutech.model.User;
import edutech.model.service.AuthService;
import edutech.view.LoginView;

/**
 * AuthController
 *
 * Purpose: Connects login input/output with the authentication service.
 * Keeps UI input and business validation separate (MVC, FR-01, NFR-01).
 *
 * @author Md Abdul Kaium
 */
public final class AuthController {
    private final LoginView view;
    private final AuthService authService;

    /**
     * Builds the login controller using its view and service.
     * @param view login view
     * @param authService authentication rules
     */
    public AuthController(LoginView view, AuthService authService) {
        if (view == null || authService == null) {
            throw new IllegalArgumentException("Login view and auth service are required.");
        }
        this.view = view;
        this.authService = authService;
    }

    /**
     * Repeats the login prompt until success or the user chooses to stop.
     * @return logged-in user; null when the user gives up
     */
    public User handleLogin() {
        while (true) {
            if (view.chooseAction() == 2) {
                return null;
            }
            String username = view.readUsername();
            String password = view.readPassword();
            try {
                User user = authService.login(username, password);
                view.showWelcome(user.getDisplayName());
                return user;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                view.showError(exception.getMessage());
            }
        }
    }
}
