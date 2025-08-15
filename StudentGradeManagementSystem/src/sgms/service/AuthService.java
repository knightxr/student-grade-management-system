package sgms.service;

import sgms.util.CredentialManager;

/**
 * Thin authentication service delegating to {@link CredentialManager}.
 */
public class AuthService {

    public boolean login(String username, String password) {
        return CredentialManager.validateLogin(username, password);
    }

    public boolean signup(String name, String username, String password) {
        return CredentialManager.addUser(name, username, password);
    }

    public boolean reset(String username, String newPassword) {
        return CredentialManager.resetPassword(username, newPassword);
    }

    public boolean isAdminPassword(String adminPassword) {
        return CredentialManager.isAdminPassword(adminPassword);
    }

    public boolean isUsernameExists(String username) {
        return CredentialManager.isUsernameExists(username);
    }
}