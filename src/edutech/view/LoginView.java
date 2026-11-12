package edutech.view;

/**
 * LoginView
 *
 * Purpose: Shows the sign-in screen and obtains user input through the one
 * shared InputHelper. MVC view for FR-01, NFR-01 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public final class LoginView {
    private final InputHelper input;

    /**
     * Creates the login view using the shared input helper.
     * @param input helper owned by Main
     */
    public LoginView(InputHelper input) {
        if (input == null) {
            throw new IllegalArgumentException("Input helper is required.");
        }
        this.input = input;
    }

    /** @return 1 for login or 2 for exit/back */
    public int chooseAction() {
        System.out.println("\n=== EduTech LMS ===");
        System.out.println("1. Log in");
        System.out.println("2. Back / exit");
        return input.readIntInRange("Choose: ", 1, 2);
    }

    /** @return supplied username */
    public String readUsername() {
        return input.readNonEmptyText("Username: ");
    }

    /**
     * Reads the password without echoing it in a terminal.
     * @return candidate password
     */
    public String readPassword() {
        // Hidden when run in a terminal; see InputHelper.readPassword
        return input.readPassword("Password: ");
    }

    /** @param userLabel display label for the logged-in user */
    public void showWelcome(String userLabel) {
        System.out.println("Welcome, " + userLabel + ".");
    }

    /** @param message readable error message */
    public void showError(String message) {
        System.out.println("Login not completed: " + message);
    }
}
