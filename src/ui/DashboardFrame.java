package ui;

import database.DatabaseHandler;
import database.UserSession;
import models.Customer;
import models.RegularCustomer;
import models.Reseller;
import utils.ThemeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DashboardFrame extends JFrame {
    
    public DashboardFrame() {
        if ("Customer".equals(UserSession.role)) {
            JOptionPane.showMessageDialog(this, "Access Denied.");
            return;
        }

        setTitle("AquaFlow Pro - Admin Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtils.BOLD_FONT);
        
        tabs.addTab(" New Transaction ", createPOSPanel());
        tabs.addTab(" Manage Customers ", createAdminPanel());

        add(tabs);
        setVisible(true);
    }

    private JPanel createPOSPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        ThemeUtils.stylePanel(panel);

        JPanel form = new JPanel(new GridLayout(5, 1, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(30, 100, 30, 100));
        form.setBackground(ThemeUtils.BACKGROUND);

        JComboBox<String> custBox = new JComboBox<>();
        loadCustomers(custBox);
        custBox.setBorder(BorderFactory.createTitledBorder("Select Customer"));
        
        JTextField qtyField = new JTextField();
        qtyField.setBorder(BorderFactory.createTitledBorder("Gallons"));
        
        JCheckBox returnBox = new JCheckBox("Returning Bottles?");
        returnBox.setBackground(ThemeUtils.BACKGROUND);

        JButton btn = new JButton("Process Order");
        ThemeUtils.styleButton(btn, ThemeUtils.PRIMARY);

        form.add(custBox); form.add(qtyField); form.add(returnBox); form.add(btn);
        panel.add(form, BorderLayout.CENTER);

        btn.addActionListener(e -> {
            try {
                String sel = (String) custBox.getSelectedItem();
                int id = Integer.parseInt(sel.split(" - ")[0]);
                int qty = Integer.parseInt(qtyField.getText());
                String type = sel.contains("Reseller") ? "Reseller" : "Regular";
                
                processTransaction(id, qty, type, returnBox.isSelected());
            } catch(Exception ex) { JOptionPane.showMessageDialog(this, "Invalid Input"); }
        });
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Name", "Bottles Owed", "Username"}, 0);
        JTable table = new JTable(model);
        refreshTable(model);

        JButton refresh = new JButton("Refresh");
        ThemeUtils.styleButton(refresh, ThemeUtils.SECONDARY);
        
        JButton delete = new JButton("Delete Selected");
        ThemeUtils.styleButton(delete, ThemeUtils.DANGER);

        JPanel btns = new JPanel();
        btns.add(refresh); btns.add(delete);
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshTable(model));
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row != -1) deleteCustomer((int)model.getValueAt(row, 0));
        });

        return panel;
    }

    // --- Logic Helpers ---
    private void loadCustomers(JComboBox<String> box) {
        try {
            ResultSet rs = DatabaseHandler.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM customers");
            while(rs.next()) box.addItem(rs.getInt(1) + " - " + rs.getString(2) + " (" + rs.getString(3) + ")");
        } catch(Exception e) {}
    }

    private void refreshTable(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            ResultSet rs = DatabaseHandler.getInstance().getConnection().createStatement().executeQuery("SELECT * FROM customers");
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(4), rs.getString(5)});
        } catch(Exception e) {}
    }

    private void processTransaction(int id, int qty, String type, boolean isReturn) throws SQLException {
        Connection conn = DatabaseHandler.getInstance().getConnection();
        if (isReturn) {
            conn.createStatement().executeUpdate("UPDATE customers SET bottles_owed = bottles_owed - " + qty + " WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Bottles Returned!");
        } else {
            Customer c = type.equals("Reseller") ? new Reseller("x") : new RegularCustomer("x");
            double price = c.calculateTotal(qty);
            
            PreparedStatement ps = conn.prepareStatement("INSERT INTO transactions (customer_id, user_id, gallons, amount) VALUES (?,?,?,?)");
            ps.setInt(1, id); ps.setInt(2, UserSession.userId); ps.setInt(3, qty); ps.setDouble(4, price);
            ps.executeUpdate();
            
            conn.createStatement().executeUpdate("UPDATE customers SET bottles_owed = bottles_owed + " + qty + " WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Sold! Total: P " + price);
        }
    }

    private void deleteCustomer(int id) {
        try {
            DatabaseHandler.getInstance().getConnection().createStatement().executeUpdate("DELETE FROM customers WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Deleted.");
        } catch(Exception e) { e.printStackTrace(); }
    }
}