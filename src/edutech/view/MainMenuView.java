package edutech.view;

/**
 * MainMenuView
 *
 * Purpose: Prints the application-level messages that do not belong to one
 * slice: the start-up banner, the demo accounts, the log-out notice and the
 * closing message. It reads no input; the login menu itself is in LoginView.
 *
 * Owner: Tasbiul Huda Tawhid (Slice C).
 *
 * @author Tasbiul Huda Tawhid
 */
public class MainMenuView {

    /**
     * Shows the application title and the demo accounts a marker can use.
     *
     * @param demoAccounts lines describing each demo account, one per line
     */
    public void showBanner(String[] demoAccounts) {
        System.out.println("==============================================");
        System.out.println(" EduTech LMS: Secure Login and Course Enrolment");
        System.out.println("==============================================");
        System.out.println("Demo accounts (username / password):");
        for (String account : demoAccounts) {
            System.out.println("  " + account);
        }
    }

    /**
     * Confirms that the current user has logged out.
     *
     * @param displayName the display name of the user who logged out
     */
    public void showLoggedOut(String displayName) {
        System.out.println(displayName + " has logged out.");
    }

    /**
     * Shows a general information or error message.
     *
     * @param message the text to show
     */
    public void showMessage(String message) {
        System.out.println(message);
    }

    /**
     * Shows the closing message when the program ends.
     */
    public void showGoodbye() {
        System.out.println("Goodbye from EduTech LMS.");
    }
}
