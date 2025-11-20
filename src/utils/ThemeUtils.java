package utils;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border; // Required for BorderFactory usage
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

// Utility class for consistent styling across the application.
public class ThemeUtils {

    // Color Palette
    public static final Color PRIMARY = new Color(0, 150, 200); // A bright, clean aqua blue
    public static final Color SECONDARY = new Color(255, 100, 100); // A soft contrasting color
    public static final Color TEXT = new Color(51, 51, 51); // Dark gray for readability
    public static final Color BACKGROUND = new Color(240, 240, 240); // Light background
    public static final Color DANGER = new Color(220, 53, 69); // Red

    // NEW: Consistent Background for input fields/forms (using BACKGROUND from your
    // set)
    public static final Color BG_LIGHT = BACKGROUND;

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 36);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    /**
     * Applies standard styling to a JButton.
     * * @param button The button to style.
     * 
     * @param bgColor The background color of the button.
     */
    public static void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(BOLD_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 1));

        // Add a hover effect for better UX
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    /**
     * Applies standard styling to a JTable.
     * * @param table The table to style.
     */
    public static void styleTable(JTable table) {
        // Table Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setBorder(null);

        // Table Row Styling
        table.setFont(NORMAL_FONT);
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(200, 230, 255)); // Light blue selection
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);

        // Center alignment for all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static void stylePanel(JPanel panel) {
        panel.setBackground(BACKGROUND);
    }

    /**
     * Creates a styled JTextField with a titled border for clear labeling.
     */
    public static JTextField createStyledTextField(String title) {
        JTextField field = new JTextField();
        field.setFont(NORMAL_FONT);
        field.setPreferredSize(new Dimension(300, 50));
        field.setBackground(Color.WHITE);
        field.setBorder(createTitledBorder(title));
        return field;
    }

    /**
     * Creates a styled JPasswordField with a titled border for clear labeling.
     */
    public static JPasswordField createStyledPasswordField(String title) {
        JPasswordField field = new JPasswordField();
        field.setFont(NORMAL_FONT);
        field.setPreferredSize(new Dimension(300, 50));
        field.setBackground(Color.WHITE);
        field.setBorder(createTitledBorder(title));
        return field;
    }

    /**
     * Helper to create a consistent TitledBorder style.
     */
    private static Border createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), // Using a slightly darker gray line
                title,
                0,
                0,
                NORMAL_FONT.deriveFont(Font.PLAIN, 12),
                TEXT);
    }
}