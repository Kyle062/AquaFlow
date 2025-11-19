package main;

import ui.LoginFrame;
import javax.swing.UIManager;

public class AquaLauncher {
    public static void main(String[] args) {
        // Make it look like Windows/Mac native app
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        // Launch the Login Screen
        new LoginFrame();
    }
}