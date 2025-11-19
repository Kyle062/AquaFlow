package utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ThemeUtils {
    public static final Color PRIMARY = new Color(0, 123, 255); // Blue
    public static final Color SECONDARY = new Color(108, 117, 125); // Gray
    public static final Color SUCCESS = new Color(40, 167, 69); // Green
    public static final Color DANGER = new Color(220, 53, 69); // Red
    public static final Color BACKGROUND = new Color(248, 249, 250); // Off-White

    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Color TEXT = new Color(33, 37, 41);
    public static final Color BACKGROUND_BUTTONS = new Color(101, 230, 255); 

    public static void styleButton(JButton btn, Color color) {
        btn.setBackground(BACKGROUND_BUTTONS);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setFont(BOLD_FONT);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
    }

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
    }
}