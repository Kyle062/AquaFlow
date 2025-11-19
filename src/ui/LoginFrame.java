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
        setSize(350, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        ThemeUtils.stylePanel(panel);

        JLabel title = new JLabel("AquaFlow", SwingConstants.CENTER);
        title.setFont(ThemeUtils.HEADER_FONT);
        title.setForeground(ThemeUtils.PRIMARY);

        JTextField userField = new JTextField();
        userField.setBorder(BorderFactory.createTitledBorder("Username"));
        
        JPasswordField passField = new JPasswordField();
        passField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginBtn = new JButton("Login");
        ThemeUtils.styleButton(loginBtn, ThemeUtils.PRIMARY);
        
        JButton regBtn = new JButton("New Customer?");
        ThemeUtils.styleButton(regBtn, ThemeUtils.SECONDARY);

        panel.add(title); panel.add(userField); panel.add(passField);
        panel.add(loginBtn); panel.add(regBtn);
        add(panel);

        regBtn.addActionListener(e -> new RegisterFrame());

        loginBtn.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            if(checkAdmin(u, p)) return;
            checkCustomer(u, p);
        });

        setVisible(true);
    }

    private boolean checkAdmin(String u, String p) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, u); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserSession.userId = rs.getInt("user_id");
                UserSession.role = rs.getString("role");
                UserSession.username = rs.getString("username");
                new DashboardFrame();
                this.dispose();
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    private void checkCustomer(String u, String p) {
        try {
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM customers WHERE username=? AND password=?");
            ps.setString(1, u); ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserSession.userId = rs.getInt("customer_id");
                UserSession.role = "Customer";
                UserSession.username = rs.getString("name");
                JOptionPane.showMessageDialog(this, "Welcome " + UserSession.username + "\nDebt: " + rs.getInt("bottles_owed"));
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Login");
            }
        } catch (Exception e) {}
    }
}