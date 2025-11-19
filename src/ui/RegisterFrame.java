package ui;

import database.DatabaseHandler;
import utils.ThemeUtils;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {

    public RegisterFrame() {
        setTitle("AquaFlow Customer Registration");
        setSize(1400, 800);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        String sideImagePath = "src/resources/side_image.png";

        // --- Outer Background (Light Blue) ---
        JPanel bgPanel = new JPanel();
        bgPanel.setLayout(null);
        bgPanel.setBackground(new Color(197, 244, 253));
        setContentPane(bgPanel);

        // --- White Center Card (1000x600, centered at 200, 80) ---
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(null);
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(200, 80, 1000, 600);
        bgPanel.add(cardPanel);

        // --- LEFT SIDE: IMAGE WITH TITLE OVERLAY (500px wide) ---
        JLabel imageLabel = new JLabel();
        imageLabel.setBounds(0, 0, 500, 600);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(155, 239, 255));

        try {
            ImageIcon icon = new ImageIcon(sideImagePath);
            Image img = icon.getImage().getScaledInstance(500, 600, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText("<html><center>Image Not Found<br>" + sideImagePath + "</center></html>");
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        cardPanel.add(imageLabel);

        // Title overlay for the Image/Promo side
        JLabel titJLabel = new JLabel("AQUAFLOW", SwingConstants.CENTER);
        titJLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        titJLabel.setForeground(Color.WHITE);
        titJLabel.setBounds(0, 50, 500, 60);
        cardPanel.add(titJLabel);

        // --- RIGHT SIDE: FORM (500px wide) ---
        // Form elements start at X=550 (500 + 50 margin)
        int formX = 550;
        int width = 350; // Match LoginFrame field width
        int fieldHeight = 45; // Match LoginFrame field height

        // Main Title
        JLabel title = new JLabel("Create Customer Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(ThemeUtils.TEXT);
        title.setBounds(formX, 40, width, 50);
        cardPanel.add(title);

        // PROJECT DESCRIPTION
        JLabel description = new JLabel(
                "<html><p>Sign up to track your bottle debts and manage orders efficiently.</p></html>");
        description.setFont(ThemeUtils.NORMAL_FONT);
        description.setForeground(Color.DARK_GRAY);
        description.setBounds(formX, 95, width, 40);
        cardPanel.add(description);

        // --- Full Name ---
        JLabel nameLabel = new JLabel("Full Name");
        nameLabel.setFont(ThemeUtils.NORMAL_FONT);
        nameLabel.setForeground(Color.GRAY);
        nameLabel.setBounds(formX, 160, width, 20);
        cardPanel.add(nameLabel);

        JTextField nameField = new JTextField();
        nameField.setBounds(formX, 180, width, fieldHeight);
        nameField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(nameField);

        // --- Username ---
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(ThemeUtils.NORMAL_FONT);
        userLabel.setForeground(Color.GRAY);
        userLabel.setBounds(formX, 235, width, 20);
        cardPanel.add(userLabel);

        JTextField userField = new JTextField();
        userField.setBounds(formX, 255, width, fieldHeight);
        userField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(userField);

        // --- Password ---
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(ThemeUtils.NORMAL_FONT);
        passLabel.setForeground(Color.GRAY);
        passLabel.setBounds(formX, 310, width, 20);
        cardPanel.add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(formX, 330, width, fieldHeight);
        passField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(passField);

        // --- Account Type (ComboBox) ---
        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(ThemeUtils.NORMAL_FONT);
        typeLabel.setForeground(Color.GRAY);
        typeLabel.setBounds(formX, 385, width, 20);
        cardPanel.add(typeLabel);

        String[] types = { "Regular", "Reseller" };
        JComboBox<String> typeBox = new JComboBox<>(types);
        typeBox.setBounds(formX, 405, width, fieldHeight);
        typeBox.setBackground(Color.WHITE);
        typeBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cardPanel.add(typeBox);

        // --- Create Account Button ---
        JButton btn = new JButton("Create Account");
        ThemeUtils.styleButton(btn, new Color(101, 230, 255));
        btn.setBounds(formX, 480, width, 50);
        cardPanel.add(btn);

        JButton logBtn = new JButton("Create an account");
        logBtn.setContentAreaFilled(false);
        logBtn.setBorderPainted(false);
        logBtn.setFocusPainted(false);
        logBtn.setForeground(ThemeUtils.PRIMARY);
        logBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        logBtn.setBounds(535, 530, 200, 30);
        cardPanel.add(logBtn);

        // --- ACTION LISTENERS ---
        logBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        // --- ACTION LISTENER ---
        btn.addActionListener(e -> {
            try {
                if (nameField.getText().isEmpty() || userField.getText().isEmpty()
                        || passField.getPassword().length == 0) {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Connection conn = DatabaseHandler.getInstance().getConnection();
                String sql = "INSERT INTO customers (name, username, password, type, bottles_owed) VALUES (?,?,?,?,0)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nameField.getText());
                ps.setString(2, userField.getText());
                ps.setString(3, new String(passField.getPassword()));
                ps.setString(4, (String) typeBox.getSelectedItem());
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
                this.dispose();
                new LoginFrame().setVisible(true);
            } catch (SQLException ex) {
                if (ex.getSQLState().startsWith("23")) {
                    JOptionPane.showMessageDialog(this, "Error: Username is already taken.", "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Registration Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new RegisterFrame();
    }
}