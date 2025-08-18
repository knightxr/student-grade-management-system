package sgms.service;

import sgms.util.CredentialManager;

/**
 * Small service that calls {@link CredentialManager} for auth work.
 * This keeps the UI code separate from the login logic.
 */
public class AuthService {

    /** Try to log in with a username and password. */
    public boolean login(String username, String password) {
        return CredentialManager.validateLogin(username, password);
    }

    /** Create a new user account. */
    public boolean signup(String name, String username, String password) {
        return CredentialManager.addUser(name, username, password);
    }

    /** Reset a user's password. */
    public boolean reset(String username, String newPassword) {
        return CredentialManager.resetPassword(username, newPassword);
    }

    /** Check if the admin password is correct. */
    public boolean isAdminPassword(String adminPassword) {
        return CredentialManager.isAdminPassword(adminPassword);
    }

    /** Check if a username is already taken. */
    public boolean isUsernameExists(String username) {
        return CredentialManager.isUsernameExists(username);
    }
}
