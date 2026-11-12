package edutech.view;

import edutech.model.User;
import java.util.List;

/**
 * AdminView
 *
 * Purpose: Displays administrator account actions and reads values through
 * the shared InputHelper. MVC view for FR-13 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public final class AdminView {
    private final InputHelper input;

    /**
     * Builds an administrator view with Main's shared helper.
     * @param input single InputHelper instance
     */
    public AdminView(InputHelper input) {
        if (input == null) {
            throw new IllegalArgumentException("Input helper is required.");
        }
        this.input = input;
    }

    /** @return 1 create, 2 list, 3 log out */
    public int chooseAction() {
        System.out.println("\n=== Administrator Menu ===");
        System.out.println("1. Create user account");
        System.out.println("2. List user accounts");
        System.out.println("3. Log out");
        return input.readIntInRange("Choose: ", 1, 3);
    }

    /** @return 1 learner, 2 instructor, 3 administrator */
    public int chooseRole() {
        System.out.println("Account type: 1. Learner  2. Instructor  3. Administrator");
        return input.readIntInRange("Choose role: ", 1, 3);
    }

    /** @return 1 student or 2 corporate trainee */
    public int chooseLearnerType() {
        return input.readIntInRange("Learner type (1. Student, 2. Corporate trainee): ", 1, 2);
    }

    /** @param label field to prompt for; @return non-blank entry */
    public String readField(String label) {
        return input.readNonEmptyText(label + ": ");
    }

    /**
     * Reads a new account password without echoing it in a terminal.
     *
     * @param label prompt text without the trailing colon
     * @return the entered password
     */
    public String readPassword(String label) {
        return input.readPassword(label + ": ");
    }

    /** @param message success message */
    public void showSuccess(String message) {
        System.out.println(message);
    }

    /** @param message error message */
    public void showError(String message) {
        System.out.println("Account action failed: " + message);
    }

    /** @param users profiles to display to the administrator */
    public void showUsers(List<User> users) {
        System.out.println("Registered accounts (no password information is displayed):");
        for (User user : users) {
            System.out.println(" - " + user.getUserId() + " | " + user.getDisplayName());
        }
    }
}
