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

// IMPORTANT: Assuming LoginFrame exists in the 'ui' package
import ui.LoginFrame; 

public class DashboardFrame extends JFrame {
    
    // --- Class Fields for cross-panel access ---
    private DefaultTableModel customerTableModel;
    private JComboBox<String> customerComboBox;

    public DashboardFrame() {
        if ("Customer".equals(UserSession.role)) {
            JOptionPane.showMessageDialog(this, "Access Denied.");
            return;
        }

        setTitle("AquaFlow Pro - Admin Dashboard");
        // Match the size of the Login/Register frames
        setSize(1400, 800); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // --- 1. Outer Background (Light Blue) ---
        JPanel bgPanel = new JPanel();
        bgPanel.setLayout(null);
        bgPanel.setBackground(new Color(197, 244, 253));
        setContentPane(bgPanel);

        // --- 2. Central White Card (1000x600) ---
        JPanel cardPanel = new JPanel(new BorderLayout(20, 20)); // Use BorderLayout for the Tabbed Pane
        cardPanel.setBackground(Color.WHITE);
        // Positioned and sized to match Login/Register frame's central card
        cardPanel.setBounds(200, 80, 1000, 600); 
        bgPanel.add(cardPanel);
        
        // --- 3. Header Panel (Title and Logout Button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE); 
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title Label
        JLabel titleLabel = new JLabel(" AquaFlow Admin Control Panel", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ThemeUtils.TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Logout Button
        JButton logoutButton = new JButton("Logout");
        // Apply styling for a clear action button, perhaps secondary color
        ThemeUtils.styleButton(logoutButton, ThemeUtils.SECONDARY.darker()); 
        logoutButton.setPreferredSize(new Dimension(100, 35));
        
        logoutButton.addActionListener(e -> {
            // Logout logic
            UserSession.userId = -1; // Clear session data
            UserSession.role = null;
            
            // Close the current dashboard
            dispose();
            
            // Open the Login Frame
            new LoginFrame();
        });
        
        headerPanel.add(logoutButton, BorderLayout.EAST);
        cardPanel.add(headerPanel, BorderLayout.NORTH);


        // --- 4. The Tabbed Pane ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtils.BOLD_FONT);
        tabs.setOpaque(false); 
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); // Reduced top padding due to new header

        tabs.addTab(" New Transaction ", createPOSPanel());
        tabs.addTab(" Manage Customers ", createAdminPanel());

        cardPanel.add(tabs, BorderLayout.CENTER);
        
        // Initial data load for all components
        refreshData(); 
        
        setVisible(true);
    }

    /**
     * Creates the Point of Sale panel for new transactions.
     */
    private JPanel createPOSPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // Use GridBagLayout for centering
        ThemeUtils.stylePanel(panel);
        panel.setBackground(Color.WHITE); // Ensure tab content is white
        
        // Inner form panel for structure and padding
        JPanel form = new JPanel(new GridLayout(4, 1, 10, 20)); // 4 rows for elements
        form.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        form.setPreferredSize(new Dimension(500, 450));
        form.setBackground(new Color(245, 245, 245)); // Light gray background for the form area

        // --- Customer ComboBox (Assigned to class field) ---
        customerComboBox = new JComboBox<>();
        customerComboBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "Select Customer", 
            0, 
            0, 
            ThemeUtils.NORMAL_FONT.deriveFont(Font.PLAIN, 12)
        ));
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setPreferredSize(new Dimension(500, 60));
        
        JTextField qtyField = new JTextField();
        qtyField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "Gallons (Quantity)", 
            0, 
            0, 
            ThemeUtils.NORMAL_FONT.deriveFont(Font.PLAIN, 12)
        ));
        qtyField.setPreferredSize(new Dimension(500, 60));
        
        JCheckBox returnBox = new JCheckBox(" Customer is returning bottles (Debt reduction)?");
        returnBox.setBackground(new Color(245, 245, 245));
        returnBox.setFont(ThemeUtils.NORMAL_FONT.deriveFont(Font.BOLD, 14));

        JButton btn = new JButton("Process Order");
        ThemeUtils.styleButton(btn, new Color(101, 230, 255)); 
        btn.setPreferredSize(new Dimension(500, 50));

        form.add(customerComboBox); 
        form.add(qtyField); 
        form.add(returnBox); 
        form.add(btn);
        
        panel.add(form); 

        btn.addActionListener(e -> {
            try {
                String sel = (String) customerComboBox.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(this, "Please select a customer.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int id = Integer.parseInt(sel.split(" - ")[0]);
                int qty = Integer.parseInt(qtyField.getText());
                String type = sel.substring(sel.lastIndexOf('(') + 1, sel.lastIndexOf(')'));
                
                processTransaction(id, qty, type, returnBox.isSelected());
                qtyField.setText("");
                returnBox.setSelected(false);
                refreshData(); // Refresh both table and dropdown after a transaction
            } catch(NumberFormatException ex) { 
                JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Input Error", JOptionPane.ERROR_MESSAGE); 
            } catch(Exception ex) { 
                JOptionPane.showMessageDialog(this, "An unexpected error occurred during transaction processing.", "Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
        return panel;
    }

    /**
     * Creates the Admin panel for managing customers.
     */
    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // --- Table Setup (Assigned to class field) ---
        customerTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Bottles Owed", "Username", "Type"}, 0);
        JTable table = new JTable(customerTableModel);
        
        // Styling the table and scroll pane
        table.setFont(ThemeUtils.NORMAL_FONT);
        table.getTableHeader().setFont(ThemeUtils.BOLD_FONT);
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        
        // Button Panel - Styled
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btns.setBackground(new Color(240, 240, 240));
        
        JButton refresh = new JButton("Refresh Customer List");
        ThemeUtils.styleButton(refresh, ThemeUtils.SECONDARY);
        
        JButton delete = new JButton("Delete Selected Customer");
        ThemeUtils.styleButton(delete, ThemeUtils.DANGER);

        btns.add(refresh); btns.add(delete);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshData()); // Call unified refresh
        delete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int id = (int)customerTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete customer ID " + id + "? This cannot be undone.", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteCustomer(id);
                    refreshData(); // Refresh all components after deletion
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }
    
    /**
     * Unified method to refresh all customer-related data components on the dashboard.
     */
    private void refreshData() {
        refreshTable();
        loadCustomers();
    }

    // --- Logic Helpers ---

    /**
     * Loads customer data into the JComboBox field.
     */
    private void loadCustomers() {
        if (customerComboBox == null) return;
        customerComboBox.removeAllItems();
        try (
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT customer_id, name, type FROM customers")
        ) {
            while(rs.next()) {
                customerComboBox.addItem(rs.getInt(1) + " - " + rs.getString(2) + " (" + rs.getString(3) + ")");
            }
        } catch(Exception e) { 
            System.err.println("Error loading customers into ComboBox: " + e.getMessage());
        }
    }

    /**
     * Loads customer data into the DefaultTableModel field.
     */
    private void refreshTable() {
        if (customerTableModel == null) return;
        customerTableModel.setRowCount(0);
        try (
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT customer_id, name, bottles_owed, username, type FROM customers")
        ) {
            // Columns: (1)ID, (2)Name, (3)Bottles_Owed, (4)Username, (5)Type
            while(rs.next()) {
                customerTableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5)});
            }
        } catch(Exception e) { 
             System.err.println("Error refreshing customer table: " + e.getMessage());
        }
    }

    private void processTransaction(int id, int qty, String type, boolean isReturn) throws SQLException {
        Connection conn = DatabaseHandler.getInstance().getConnection();
        if (isReturn) {
            PreparedStatement getDebt = conn.prepareStatement("SELECT bottles_owed FROM customers WHERE customer_id=?");
            getDebt.setInt(1, id);
            ResultSet rs = getDebt.executeQuery();
            if (rs.next()) {
                int currentDebt = rs.getInt("bottles_owed");
                if (currentDebt < qty) {
                    JOptionPane.showMessageDialog(this, "Customer is returning more bottles than owed (" + currentDebt + ").", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            conn.createStatement().executeUpdate("UPDATE customers SET bottles_owed = bottles_owed - " + qty + " WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Successfully processed " + qty + " bottles returned (Debt reduced).");
        } else {
            // Instantiate the correct model type
            Customer c = type.equals("Reseller") ? new Reseller("x") : new RegularCustomer("x");
            double price = c.calculateTotal(qty);
            
            PreparedStatement ps = conn.prepareStatement("INSERT INTO transactions (customer_id, user_id, gallons, amount) VALUES (?,?,?,?)");
            ps.setInt(1, id); ps.setInt(2, UserSession.userId); ps.setInt(3, qty); ps.setDouble(4, price);
            ps.executeUpdate();
            
            conn.createStatement().executeUpdate("UPDATE customers SET bottles_owed = bottles_owed + " + qty + " WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Sale Processed! Total cost: P " + String.format("%.2f", price) + ".\n" + qty + " bottles added to debt.");
        }
    }

    // This method handles the database deletion only
    private void deleteCustomer(int id) {
        try {
            DatabaseHandler.getInstance().getConnection().createStatement()
                    .executeUpdate("DELETE FROM customers WHERE customer_id=" + id);
            JOptionPane.showMessageDialog(this, "Customer ID " + id + " successfully deleted.");
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}