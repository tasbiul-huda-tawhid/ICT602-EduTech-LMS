package edutech.controller;

import edutech.model.Administrator;
import edutech.model.Instructor;
import edutech.model.Learner;
import edutech.model.LearnerType;
import edutech.model.Role;
import edutech.model.User;
import edutech.model.service.AuthService;
import edutech.view.AdminView;

/**
 * AdminController
 *
 * Purpose: Coordinates administrator account creation and listing.
 * Role is checked before the menu is shown (FR-13, NFR-01).
 *
 * @author Md Abdul Kaium
 */
public final class AdminController {
    private final AdminView view;
    private final AuthService authService;

    /**
     * Builds the administrator controller.
     * @param view administrator input/output
     * @param authService account-registration service
     */
    public AdminController(AdminView view, AuthService authService) {
        if (view == null || authService == null) {
            throw new IllegalArgumentException("Admin view and auth service are required.");
        }
        this.view = view;
        this.authService = authService;
    }

    /**
     * Displays authorised administration actions until log out.
     * @param admin authenticated administrator profile
     * @throws IllegalArgumentException if the supplied profile is invalid
     */
    public void runAdminMenu(Administrator admin) {
        if (admin == null || admin.getRole() != Role.ADMINISTRATOR) {
            throw new IllegalArgumentException("Administrator access required.");
        }
        while (true) {
            int selection = view.chooseAction();
            if (selection == 3) {
                return;
            }
            if (selection == 2) {
                view.showUsers(authService.getRegisteredUsers());
                continue;
            }
            try {
                User newUser = collectUserDetails();
                String username = view.readField("Login username");
                String password = view.readPassword("Password (minimum 8 characters)");
                authService.registerUser(newUser, username, password);
                view.showSuccess("Created " + newUser.getDisplayName() + " successfully.");
            } catch (IllegalArgumentException | IllegalStateException exception) {
                view.showError(exception.getMessage());
            }
        }
    }

    /** Gathers role-specific user fields, then delegates validation to models. */
    private User collectUserDetails() {
        int role = view.chooseRole();
        String userId = view.readField("User ID");
        String name = view.readField("Full name");
        String email = view.readField("Email");
        if (role == 1) {
            int learnerType = view.chooseLearnerType();
            return new Learner(userId, name, email,
                    learnerType == 1 ? LearnerType.STUDENT : LearnerType.CORPORATE_TRAINEE);
        }
        if (role == 2) {
            String department = view.readField("Teaching department");
            return new Instructor(userId, name, email, department);
        }
        return new Administrator(userId, name, email);
    }
}
