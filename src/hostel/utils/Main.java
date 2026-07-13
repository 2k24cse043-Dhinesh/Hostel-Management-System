package hostel.utils;

import hostel.utils.LoginChoiceFrame;

/**
 * Entry point — launches the Login Choice screen.
 */
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new LoginChoiceFrame().setVisible(true);
        });
    }
}