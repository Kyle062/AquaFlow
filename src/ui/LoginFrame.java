package ui;

import database.DatabaseHandler;
import database.UserSession;
import utils.ThemeUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("AquaFlow Login");
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        String sideImagePath = "src/resources/side_image.png";

        JPanel bgPanel = new JPanel();
        bgPanel.setLayout(null);
        bgPanel.setBackground(new Color(197, 244, 253));
        setContentPane(bgPanel);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(null);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(200, 80, 1000, 600);
        bgPanel.add(cardPanel);

        // --- LEFT SIDE: IMAGE WITH TITLE OVERLAY ---
        JLabel titJLabel = new JLabel("AQUAFLOW", SwingConstants.CENTER);
        titJLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titJLabel.setForeground(ThemeUtils.PRIMARY);
        titJLabel.setBounds(0, 45, 500, 60);
        cardPanel.add(titJLabel);

        JLabel imageLabel = new JLabel();
        imageLabel.setBounds(0, 0, 500, 600);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(155, 239, 255));

        try {
            ImageIcon icon = new ImageIcon(sideImagePath);
            Image img = icon.getImage().getScaledInstance(500, 550, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));

        } catch (Exception e) {
            imageLabel.setText("<html><center>Image Not Found<br>" + sideImagePath + "</center></html>");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }

        cardPanel.add(imageLabel);

        // --- RIGHT SIDE: FORM ---

        // Main Title
        JLabel title = new JLabel("Welcome Back!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(ThemeUtils.TEXT);
        title.setBounds(550, 40, 350, 50);
        cardPanel.add(title);

        // PROJECT DESCRIPTION
        JLabel description = new JLabel(
                "<html><p>Water refilling station management system, tracking customer debts and inventory.</p></html>");
        description.setFont(ThemeUtils.BOLD_FONT);
        description.setForeground(Color.DARK_GRAY);
        description.setBounds(550, 95, 350, 40);
        cardPanel.add(description);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeUtils.NORMAL_FONT);
        userLabel.setForeground(Color.GRAY);
        userLabel.setBounds(550, 160, 100, 30);
        cardPanel.add(userLabel);

        JTextField userField = new JTextField();
        userField.setBounds(550, 190, 350, 45);
        userField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(userField);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeUtils.NORMAL_FONT);
        passLabel.setForeground(Color.GRAY);
        passLabel.setBounds(550, 250, 100, 30);
        cardPanel.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(550, 280, 350, 45);
        passField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(passField);

        // Login Button
        JButton loginBtn = new JButton("Log in");
        // Style using custom color directly
        ThemeUtils.styleButton(loginBtn, new Color(101, 230, 255));
        loginBtn.setBounds(550, 370, 350, 50);
        cardPanel.add(loginBtn);

        // Register Link
        JButton regBtn = new JButton("Create an account");
        regBtn.setContentAreaFilled(false);
        regBtn.setBorderPainted(false);
        regBtn.setFocusPainted(false);
        regBtn.setForeground(ThemeUtils.PRIMARY);
        regBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        regBtn.setHorizontalAlignment(SwingConstants.LEFT);
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        regBtn.setBounds(535, 440, 200, 30);
        cardPanel.add(regBtn);

        // --- ACTION LISTENERS ---
        regBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        loginBtn.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            if (checkAdmin(u, p))
                return;
            checkCustomer(u, p);
        });

        setVisible(true);
    }

    private boolean checkAdmin(String u, String p) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserSession.userId = rs.getInt("user_id");
                UserSession.role = rs.getString("role");
                UserSession.username = rs.getString("username");
                new DashboardFrame();
                this.dispose();
                return true;
            }
        } catch (Exception e) {
            System.err.println("Admin check failed: " + e.getMessage());
        }
        return false;
    }

    private void checkCustomer(String u, String p) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers WHERE username=? AND password=?");
            ps.setString(1, u);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserSession.userId = rs.getInt("customer_id");
                UserSession.role = "Customer";
                UserSession.username = rs.getString("name");

                new CustomerDashboardFrame().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }
        } catch (Exception e) {
            System.err.println("Customer check failed: " + e.getMessage());
        }
    }
}