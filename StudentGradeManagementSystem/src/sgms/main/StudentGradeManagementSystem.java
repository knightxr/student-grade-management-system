package sgms.main;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import javax.swing.UIManager;
import sgms.ui.LoginPage;

/**
 * Entry point for the Student Grade Management System application. Sets the UI theme and
 * launches the login page.
 */
public class StudentGradeManagementSystem {

    /**
     * Main method to start the application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
        } catch (Exception ex) {
            // Ignore exceptions related to look and feel initialization
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginPage().setVisible(true);
            }
        });
    }
}