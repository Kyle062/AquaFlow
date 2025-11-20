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
import java.util.logging.Level;
import java.util.logging.Logger;

// IMPORTANT: Assuming LoginFrame exists in the 'ui' package
import ui.LoginFrame; 

public class DashboardFrame extends JFrame {
    
    // --- Class Fields for cross-panel access ---
    private static final Logger LOGGER = Logger.getLogger(DashboardFrame.class.getName());

    private DefaultTableModel customerTableModel;
    private DefaultTableModel userTableModel; // NEW: Table model for system users
    private JComboBox<String> customerComboBox;
    
    // NEW: Fields for User Management Tab
    private JTextField searchUserField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTable userTable;
    private JButton updateButton;
    private JButton deleteButton;

    public DashboardFrame() {
        if (!"Admin".equals(UserSession.role)) { // Ensure only Admins access this panel
             // Handle case where Admin logs out (UserSession.role might be null)
            if (!"Customer".equals(UserSession.role)) { 
                JOptionPane.showMessageDialog(this, "Session Invalid or Role Missing. Redirecting to Login.");
                dispose();
                new LoginFrame();
                return;
            }
            JOptionPane.showMessageDialog(this, "Access Denied. Only Administrators can use this panel.");
            dispose();
            new CustomerDashboardFrame(); // Redirect to the customer view instead of denying access
            return;
        }

        setTitle("AquaFlow Pro - Admin Dashboard");
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
        JPanel cardPanel = new JPanel(new BorderLayout(20, 20)); 
        cardPanel.setBackground(Color.WHITE);
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
        ThemeUtils.styleButton(logoutButton, ThemeUtils.SECONDARY.darker()); 
        logoutButton.setPreferredSize(new Dimension(100, 35));
        
        logoutButton.addActionListener(e -> {
            UserSession.userId = -1; 
            UserSession.role = null;
            DatabaseHandler.getInstance().closeConnection(); // Close connection on logout
            dispose();
            new LoginFrame();
        });
        
        headerPanel.add(logoutButton, BorderLayout.EAST);
        cardPanel.add(headerPanel, BorderLayout.NORTH);


        // --- 4. The Tabbed Pane ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtils.BOLD_FONT);
        tabs.setOpaque(false); 
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20)); 

        tabs.addTab(" New Transaction ", createPOSPanel());
        tabs.addTab(" Manage Customers ", createAdminPanel());
        tabs.addTab(" Manage System Users ", createUsersPanel()); // NEW TAB

        cardPanel.add(tabs, BorderLayout.CENTER);
        
        refreshData(); 
        
        setVisible(true);
    }

    // =========================================================================
    // POS and Customer Management (Existing Logic)
    // =========================================================================

    private JPanel createPOSPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); 
        ThemeUtils.stylePanel(panel);
        panel.setBackground(Color.WHITE); 
        
        JPanel form = new JPanel(new GridLayout(4, 1, 10, 20)); 
        form.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        form.setPreferredSize(new Dimension(500, 450));
        form.setBackground(new Color(245, 245, 245)); 

        customerComboBox = new JComboBox<>();
        customerComboBox.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "Select Customer", 
            0, 0, ThemeUtils.NORMAL_FONT.deriveFont(Font.PLAIN, 12)
        ));
        customerComboBox.setBackground(Color.WHITE);
        customerComboBox.setPreferredSize(new Dimension(500, 60));
        
        JTextField qtyField = new JTextField();
        qtyField.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "Gallons (Quantity)", 
            0, 0, ThemeUtils.NORMAL_FONT.deriveFont(Font.PLAIN, 12)
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
                refreshData(); 
            } catch(NumberFormatException ex) { 
                JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Input Error", JOptionPane.ERROR_MESSAGE); 
            } catch(Exception ex) { 
                LOGGER.log(Level.SEVERE, "Transaction Processing Error", ex);
                JOptionPane.showMessageDialog(this, "An unexpected error occurred during transaction processing.", "Error", JOptionPane.ERROR_MESSAGE); 
            }
        });
        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        customerTableModel = new DefaultTableModel(new String[]{"ID", "Name", "Bottles Owed", "Username", "Type"}, 0);
        JTable table = new JTable(customerTableModel);
        
        table.setFont(ThemeUtils.NORMAL_FONT);
        table.getTableHeader().setFont(ThemeUtils.BOLD_FONT);
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        btns.setBackground(new Color(240, 240, 240));
        
        JButton refresh = new JButton("Refresh Customer List");
        ThemeUtils.styleButton(refresh, ThemeUtils.SECONDARY);
        
        JButton delete = new JButton("Delete Selected Customer");
        ThemeUtils.styleButton(delete, ThemeUtils.DANGER);

        btns.add(refresh); btns.add(delete);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(btns, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshData()); 
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
                    refreshData(); 
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    private void refreshData() {
        refreshCustomerTable();
        loadCustomersForComboBox();
        refreshUserTable(null); // Load all users initially
    }
    
    // Updated helper name
    private void loadCustomersForComboBox() {
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
            LOGGER.log(Level.SEVERE, "Error loading customers into ComboBox.", e);
        }
    }

    // Updated helper name
    private void refreshCustomerTable() {
        if (customerTableModel == null) return;
        customerTableModel.setRowCount(0);
        try (
            Connection conn = DatabaseHandler.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT customer_id, name, bottles_owed, username, type FROM customers")
        ) {
            while(rs.next()) {
                customerTableModel.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5)});
            }
        } catch(Exception e) { 
             LOGGER.log(Level.SEVERE, "Error refreshing customer table.", e);
        }
    }

    private void processTransaction(int id, int qty, String type, boolean isReturn) throws SQLException {
        Connection conn = DatabaseHandler.getInstance().getConnection();
        conn.setAutoCommit(false); // Start transaction
        try {
            if (isReturn) {
                PreparedStatement getDebt = conn.prepareStatement("SELECT bottles_owed FROM customers WHERE customer_id=?");
                getDebt.setInt(1, id);
                ResultSet rs = getDebt.executeQuery();
                int currentDebt = 0;
                if (rs.next()) {
                    currentDebt = rs.getInt("bottles_owed");
                }
                rs.close();
                getDebt.close();

                if (currentDebt < qty) {
                    JOptionPane.showMessageDialog(this, "Customer is returning more bottles than owed (" + currentDebt + ").", "Error", JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                    return;
                }
                
                String updateSql = "UPDATE customers SET bottles_owed = bottles_owed - ? WHERE customer_id=?";
                PreparedStatement updateDebt = conn.prepareStatement(updateSql);
                updateDebt.setInt(1, qty);
                updateDebt.setInt(2, id);
                updateDebt.executeUpdate();
                updateDebt.close();
                
                JOptionPane.showMessageDialog(this, "Successfully processed " + qty + " bottles returned (Debt reduced).");
            } else {
                Customer c = type.equals("Reseller") ? new Reseller("x") : new RegularCustomer("x");
                double price = c.calculateTotal(qty);
                
                String insertTxnSql = "INSERT INTO transactions (customer_id, user_id, gallons, amount, transaction_date) VALUES (?,?,?,?, NOW())";
                PreparedStatement ps = conn.prepareStatement(insertTxnSql);
                ps.setInt(1, id); 
                ps.setInt(2, UserSession.userId); // Admin/Manager ID performing the sale
                ps.setInt(3, qty); 
                ps.setDouble(4, price);
                ps.executeUpdate();
                ps.close();
                
                String updateDebtSql = "UPDATE customers SET bottles_owed = bottles_owed + ? WHERE customer_id=?";
                PreparedStatement updateDebt = conn.prepareStatement(updateDebtSql);
                updateDebt.setInt(1, qty);
                updateDebt.setInt(2, id);
                updateDebt.executeUpdate();
                updateDebt.close();
                
                JOptionPane.showMessageDialog(this, "Sale Processed! Total cost: P " + String.format("%.2f", price) + ".\n" + qty + " bottles added to debt.");
            }
            conn.commit(); // Commit transaction
        } catch (SQLException e) {
            conn.rollback(); // Rollback on error
            throw e; // Re-throw to be caught by the ActionListener
        } finally {
            conn.setAutoCommit(true); // Restore default
        }
    }

    private void deleteCustomer(int id) {
        // NOTE: In a production system, deleting a customer should also delete related records 
        // (e.g., transactions) or be blocked if such records exist.
        try (Connection conn = DatabaseHandler.getInstance().getConnection()) {
            // Delete customer's transactions first to satisfy foreign key constraints
            PreparedStatement deleteTxn = conn.prepareStatement("DELETE FROM transactions WHERE customer_id = ?");
            deleteTxn.setInt(1, id);
            deleteTxn.executeUpdate();
            
            // Delete the customer
            PreparedStatement deleteCust = conn.prepareStatement("DELETE FROM customers WHERE customer_id = ?");
            deleteCust.setInt(1, id);
            deleteCust.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Customer ID " + id + " and all related transactions successfully deleted.");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting customer ID: " + id, e);
            JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // =========================================================================
    // NEW: User Management Panel (System Users: Admin, Driver, etc.)
    // =========================================================================

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        // --- Left: User Management Form (CRUD) ---
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ThemeUtils.PRIMARY.darker()), 
                "User Details", 
                0, 0, ThemeUtils.BOLD_FONT.deriveFont(Font.BOLD, 16), ThemeUtils.TEXT));
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setPreferredSize(new Dimension(350, 600)); // Fixed width for form

        // Initialize fields
        usernameField = ThemeUtils.createStyledTextField("Username");
        passwordField = ThemeUtils.createStyledPasswordField("Password (Set New)");
        roleComboBox = new JComboBox<>(new String[]{"Admin", "Manager", "Driver"});
        roleComboBox.setBorder(BorderFactory.createTitledBorder("Role"));
        roleComboBox.setFont(ThemeUtils.NORMAL_FONT);
        roleComboBox.setBackground(Color.WHITE);

        JButton addButton = new JButton("Add New User");
        updateButton = new JButton("Update Selected User");
        deleteButton = new JButton("Delete Selected User");

        ThemeUtils.styleButton(addButton, ThemeUtils.PRIMARY);
        ThemeUtils.styleButton(updateButton, ThemeUtils.SECONDARY);
        ThemeUtils.styleButton(deleteButton, ThemeUtils.DANGER);
        
        // Initially disable update/delete until a user is selected
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Add padding around components
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(roleComboBox);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(addButton);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(updateButton);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(deleteButton);
        formPanel.add(Box.createVerticalGlue()); 
        
        panel.add(formPanel, BorderLayout.WEST);

        // --- Center: User List Table and Search ---
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));
        tablePanel.setBackground(Color.WHITE);

        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        searchPanel.setBackground(Color.WHITE);
        searchUserField = new JTextField(20);
        searchUserField.setFont(ThemeUtils.NORMAL_FONT);
        JButton searchButton = new JButton("Search");
        ThemeUtils.styleButton(searchButton, new Color(220, 220, 220)); 
        
        searchPanel.add(new JLabel(" Filter by Username or Role: "), BorderLayout.WEST);
        searchPanel.add(searchUserField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        tablePanel.add(searchPanel, BorderLayout.NORTH);

        // Table Setup
        userTableModel = new DefaultTableModel(new String[]{"ID", "Username", "Role"}, 0);
        userTable = new JTable(userTableModel);
        userTable.setFont(ThemeUtils.NORMAL_FONT);
        userTable.getTableHeader().setFont(ThemeUtils.BOLD_FONT);
        userTable.setRowHeight(25);
        
        JScrollPane userScrollPane = new JScrollPane(userTable);
        tablePanel.add(userScrollPane, BorderLayout.CENTER);

        panel.add(tablePanel, BorderLayout.CENTER);
        
        // --- Action Listeners ---
        
        // Row Selection Listener (Populate form and enable buttons)
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && userTable.getSelectedRow() != -1) {
                int selectedRow = userTable.getSelectedRow();
                String username = (String) userTableModel.getValueAt(selectedRow, 1);
                String role = (String) userTableModel.getValueAt(selectedRow, 2);
                
                usernameField.setText(username);
                // NOTE: Password field is intentionally cleared for security; user must re-enter to update
                passwordField.setText(""); 
                roleComboBox.setSelectedItem(role);
                
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
            } else if (userTable.getSelectedRow() == -1) {
                // Clear selection state
                usernameField.setText("");
                passwordField.setText("");
                updateButton.setEnabled(false);
                deleteButton.setEnabled(false);
            }
        });
        
        addButton.addActionListener(e -> addUser());
        updateButton.addActionListener(e -> updateUser());
        deleteButton.addActionListener(e -> deleteUser());
        searchButton.addActionListener(e -> refreshUserTable(searchUserField.getText()));

        return panel;
    }
    
    /**
     * Loads/refreshes user data (Admin, Manager, Driver) into the user table.
     * @param filterText Optional search filter (username or role). If null, loads all.
     */
    private void refreshUserTable(String filterText) {
        if (userTableModel == null) return;
        userTableModel.setRowCount(0);
        String sql = "SELECT user_id, username, role FROM users";
        
        if (filterText != null && !filterText.trim().isEmpty()) {
            sql += " WHERE username LIKE ? OR role LIKE ?";
        }
        
        try (
            Connection conn = DatabaseHandler.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            if (filterText != null && !filterText.trim().isEmpty()) {
                String pattern = "%" + filterText.trim() + "%";
                ps.setString(1, pattern);
                ps.setString(2, pattern);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userTableModel.addRow(new Object[]{rs.getInt("user_id"), rs.getString("username"), rs.getString("role")});
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error refreshing user table.", e);
        }
    }
    
    /**
     * Adds a new system user to the 'users' table.
     */
    private void addUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // NOTE: In a real app, the password MUST be hashed before storage.
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseHandler.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password); // Placeholder: Use Hashing in production!
            ps.setString(3, role);
            
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "User '" + username + "' added successfully as " + role + ".");
            // Clear fields and refresh table
            usernameField.setText("");
            passwordField.setText("");
            refreshUserTable(null);

        } catch (SQLException e) {
            // Check for duplicate key error (MySQL error code 1062)
            if (e.getErrorCode() == 1062) {
                JOptionPane.showMessageDialog(this, "Error: Username '" + username + "' already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                LOGGER.log(Level.SEVERE, "Error adding user.", e);
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Updates the username, password, and/or role of the currently selected system user.
     */
    private void updateUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return; // Should be impossible if button is enabled
        
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String newUsername = usernameField.getText().trim();
        String newPassword = new String(passwordField.getPassword());
        String newRole = (String) roleComboBox.getSelectedItem();

        if (newUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build SQL dynamically based on whether password was changed
        String sql;
        if (newPassword.isEmpty()) {
            // Update only username and role
            sql = "UPDATE users SET username = ?, role = ? WHERE user_id = ?";
        } else {
            // Update username, password, and role
            // NOTE: Placeholder: Use Hashing in production!
            sql = "UPDATE users SET username = ?, password = ?, role = ? WHERE user_id = ?";
        }

        try (Connection conn = DatabaseHandler.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newUsername);

            int paramIndex = 2;
            if (!newPassword.isEmpty()) {
                ps.setString(paramIndex++, newPassword);
            }
            
            ps.setString(paramIndex++, newRole);
            ps.setInt(paramIndex, userId);
            
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User ID " + userId + " updated successfully.");
                // Clear fields and refresh table
                usernameField.setText("");
                passwordField.setText("");
                refreshUserTable(null);
            } else {
                JOptionPane.showMessageDialog(this, "User ID " + userId + " not found or no changes made.", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user ID: " + userId, e);
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Deletes the currently selected system user from the 'users' table.
     */
    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) return; // Should be impossible if button is enabled
        
        int userId = (int) userTableModel.getValueAt(selectedRow, 0);
        String username = (String) userTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete system user '" + username + "' (ID: " + userId + ")? This cannot be undone.", 
            "Confirm System User Deletion", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM users WHERE user_id = ?";

            try (Connection conn = DatabaseHandler.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "System User '" + username + "' deleted successfully.");
                    refreshUserTable(null); // Refresh table
                } else {
                    JOptionPane.showMessageDialog(this, "System User ID " + userId + " not found.", "Warning", JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error deleting user ID: " + userId, e);
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}