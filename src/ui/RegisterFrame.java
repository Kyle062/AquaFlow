package ui;

import database.DatabaseHandler; // Importing from database folder
import utils.ThemeUtils; // Importing from utils folder
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {

    public RegisterFrame() {
        setTitle("Create Customer Account");
        setSize(400, 450);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        ThemeUtils.stylePanel(panel);

        JLabel title = new JLabel("Join AquaFlow", SwingConstants.CENTER);
        title.setFont(ThemeUtils.HEADER_FONT);
        title.setForeground(ThemeUtils.PRIMARY);

        JTextField nameField = new JTextField();
        nameField.setBorder(BorderFactory.createTitledBorder("Full Name"));

        JTextField userField = new JTextField();
        userField.setBorder(BorderFactory.createTitledBorder("Username"));

        JPasswordField passField = new JPasswordField();
        passField.setBorder(BorderFactory.createTitledBorder("Password"));

        String[] types = { "Regular", "Reseller" };
        JComboBox<String> typeBox = new JComboBox<>(types);
        typeBox.setBorder(BorderFactory.createTitledBorder("Account Type"));
        JButton bt2 = new JButton();

        JButton btn = new JButton("Create Account");
        ThemeUtils.styleButton(btn, ThemeUtils.SUCCESS);

        panel.add(title);
        panel.add(nameField);
        panel.add(userField);
        panel.add(passField);
        panel.add(typeBox);
        panel.add(btn);
        add(panel);

        btn.addActionListener(e -> {
            try {
                Connection conn = DatabaseHandler.getInstance().getConnection();
                String sql = "INSERT INTO customers (name, username, password, type, bottles_owed) VALUES (?,?,?,?,0)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nameField.getText());
                ps.setString(2, userField.getText());
                ps.setString(3, new String(passField.getPassword()));
                ps.setString(4, (String) typeBox.getSelectedItem());
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Success! Login now.");
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Username taken or DB issue.");
            }
        });

        setVisible(true);
    }
}