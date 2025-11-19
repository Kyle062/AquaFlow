package main;

import ui.LoginFrame;
import javax.swing.UIManager;

public class AquaLauncher {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }

        new LoginFrame();
    }
}