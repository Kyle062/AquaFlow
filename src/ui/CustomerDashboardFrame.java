package ui;

import database.DatabaseHandler;
import database.UserSession;
import utils.ThemeUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerDashboardFrame extends JFrame {

    // Logger for better error reporting instead of just System.err
    private static final Logger LOGGER = Logger.getLogger(CustomerDashboardFrame.class.getName());

    private JLabel bottlesOwedLabel;
    private JLabel customerNameLabel;
    private JLabel accountTypeLabel;
    private DefaultTableModel historyTableModel;

    public CustomerDashboardFrame() {
        // Essential check: Only proceed if the user is logged in as a Customer
        if (!"Customer".equals(UserSession.role) || UserSession.userId == -1) {
            JOptionPane.showMessageDialog(this, "Access Denied. Please log in as a customer.", "Security Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        setTitle("AquaFlow Pro - Customer Dashboard");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // --- 1. Outer Background (Light Blue) ---
        JPanel bgPanel = new JPanel();
        bgPanel.setLayout(null);
        bgPanel.setBackground(new Color(197, 244, 253));
        setContentPane(bgPanel);

        // --- 2. Central White Card (800x550) ---
        JPanel cardPanel = new JPanel(new BorderLayout(10, 10));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBounds(100, 50, 800, 500);
        bgPanel.add(cardPanel);

        // --- 3. Header Panel (Title and Logout Button) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Title Label
        JLabel titleLabel = new JLabel(" My AquaFlow Account", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(ThemeUtils.TEXT);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Logout Button
        JButton logoutButton = new JButton("Logout");
        ThemeUtils.styleButton(logoutButton, ThemeUtils.DANGER.darker());
        logoutButton.setPreferredSize(new Dimension(100, 35));

        logoutButton.addActionListener(e -> {
            // Logout logic
            UserSession.userId = -1; // Clear session data
            UserSession.role = null;
            dispose();
            new LoginFrame(); // Go back to login screen
            
            // CORRECTED: Call the closeConnection method on the singleton instance
            DatabaseHandler.getInstance().closeConnection();
        });

        headerPanel.add(logoutButton, BorderLayout.EAST);
        cardPanel.add(headerPanel, BorderLayout.NORTH);

        // --- 4. The Tabbed Pane ---
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(ThemeUtils.BOLD_FONT);
        tabs.setOpaque(false);
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        tabs.addTab(" Account Summary ", createSummaryPanel());
        tabs.addTab(" Transaction History ", createHistoryPanel());

        cardPanel.add(tabs, BorderLayout.CENTER);

        // Load data when the frame is initialized
        loadCustomerData();
        loadTransactionHistory();

        setVisible(true);
    }

    /**
     * Creates the panel showing customer details and current debt.
     */
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JPanel infoCard = new JPanel(new GridLayout(4, 1, 10, 25));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeUtils.SECONDARY.darker(), 2),
                BorderFactory.createEmptyBorder(30, 50, 30, 50)));
        infoCard.setBackground(new Color(240, 250, 255));
        infoCard.setPreferredSize(new Dimension(450, 350));

        Font dataFont = ThemeUtils.BOLD_FONT.deriveFont(Font.BOLD, 20);
        
        // 1. Bottles Owed (Debt)
        bottlesOwedLabel = new JLabel("Loading...");
        bottlesOwedLabel.setFont(dataFont.deriveFont(Font.BOLD, 36));
        bottlesOwedLabel.setForeground(ThemeUtils.DANGER);
        bottlesOwedLabel.setBorder(BorderFactory.createTitledBorder(
                "Current Bottles Debt (Gallons Owed)"));

        // 2. Customer Name
        customerNameLabel = new JLabel("Loading...");
        customerNameLabel.setFont(dataFont);
        customerNameLabel.setBorder(BorderFactory.createTitledBorder("Customer Name"));

        // 3. Account Type
        accountTypeLabel = new JLabel("Loading...");
        accountTypeLabel.setFont(dataFont);
        accountTypeLabel.setBorder(BorderFactory.createTitledBorder("Account Type"));

        // Add components to the card
        infoCard.add(bottlesOwedLabel);
        infoCard.add(customerNameLabel);
        infoCard.add(accountTypeLabel);

        panel.add(infoCard);
        return panel;
    }

    /**
     * Creates the panel showing the transaction history table.
     */
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // --- Table Setup ---
        // Note: The "Type" column is hardcoded in loadTransactionHistory, ideally 
        // a 'transaction_type' column should exist in the database.
        historyTableModel = new DefaultTableModel(new String[] { "Date", "Gallons", "Amount Paid (P)", "Type" }, 0);
        JTable table = new JTable(historyTableModel);

        // Styling the table and scroll pane
        table.setFont(ThemeUtils.NORMAL_FONT);
        table.getTableHeader().setFont(ThemeUtils.BOLD_FONT);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Fetches and displays core customer data (Name, Type, Bottles Owed).
     */
    private void loadCustomerData() {
        int customerId = UserSession.userId;
        // Check if the connection is valid before running the query
        Connection conn = DatabaseHandler.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Could not establish database connection.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT name, type, bottles_owed FROM customers WHERE customer_id = ?";

        // Use try-with-resources to ensure PreparedStatement and ResultSet are closed
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    int bottlesOwed = rs.getInt("bottles_owed");

                    // Update UI labels
                    customerNameLabel.setText(name);
                    accountTypeLabel.setText(type);
                    // Use String.format for cleaner presentation
                    bottlesOwedLabel.setText(String.format("%d Gal", bottlesOwed)); 
                } else {
                    JOptionPane.showMessageDialog(this, "Customer data not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading customer data for ID: " + customerId, e);
            JOptionPane.showMessageDialog(this, "Error loading customer data: " + e.getMessage(), "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fetches and displays the transaction history for the logged-in customer.
     * IMPROVEMENT: Uses SimpleDateFormat to properly format the transaction date.
     */
    private void loadTransactionHistory() {
        int customerId = UserSession.userId;
        
        Connection conn = DatabaseHandler.getInstance().getConnection();
        if (conn == null) {
            // Connection error already handled in loadCustomerData or DatabaseHandler, 
            // but we ensure we don't proceed.
            return; 
        }

        // We order by transaction_date DESC to show the latest transactions first.
        String sql = "SELECT trans_date, gallons, amount FROM transactions WHERE customer_id = ? ORDER BY trans_date DESC";

        historyTableModel.setRowCount(0); // Clear existing data

        // Date formatter for display in the table
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

        // Use try-with-resources for automatic closing of database resources
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Get the timestamp object from the database
                    Timestamp transactionTimestamp = rs.getTimestamp("trans_date");
                    int gallons = rs.getInt("gallons");
                    double amount = rs.getDouble("amount");

                    // Format the timestamp to a displayable date string
                    String formattedDate = dateFormatter.format(transactionTimestamp);

                    historyTableModel.addRow(new Object[] {
                            formattedDate,
                            gallons,
                            String.format("P %.2f", amount),
                            "Purchase" // Hardcoded: This should ideally come from a 'type' column in the transactions table
                    });
                }
                System.out.println("Transaction history loaded successfully for customer ID: " + customerId);
            }
        } catch (Exception e) { // Catching generic Exception to handle SQL and Date formatting errors
            LOGGER.log(Level.SEVERE, "Error loading transaction history for ID: " + customerId, e);
            // Non-critical error, just log and display a message in the console/log
        }
    }
}