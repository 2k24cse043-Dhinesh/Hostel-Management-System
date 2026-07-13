package hostel.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UIHelper — reusable UI components for consistent styling across all frames.
 */
public class UIHelper {

    // ── Colour Palette ───────────────────────────────────────
    public static final Color BG_DARK    = new Color(0x1a1a2e);
    public static final Color BG_PANEL   = new Color(0x16213e);
    public static final Color ACCENT     = new Color(0xe94560);
    public static final Color ACCENT2    = new Color(0x0f3460);
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color TEXT_MUTED = new Color(0xaaaacc);
    public static final Color SUCCESS    = new Color(0x4caf50);
    public static final Color WARNING    = new Color(0xffc107);
    public static final Color DANGER     = new Color(0xf44336);
    public static final Color INFO       = new Color(0x4fc3f7);

    /** Styled JTextField */
    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(0x0f3460));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return f;
    }

    /** Styled JPasswordField */
    public static JPasswordField styledPassword(int cols) {
        JPasswordField p = new JPasswordField(cols);
        p.setBackground(new Color(0x0f3460));
        p.setForeground(Color.WHITE);
        p.setCaretColor(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        p.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return p;
    }

    /** Styled JLabel */
    public static JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }

    /** Styled header label */
    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setForeground(TEXT_WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        return l;
    }

    /** Primary action button */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    /** Secondary button */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(ACCENT2);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 36));
        return btn;
    }

    /** Danger button (red) */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Dark background panel */
    public static JPanel darkPanel() {
        JPanel p = new JPanel();
        p.setBackground(BG_DARK);
        return p;
    }

    /** Panel border */
    public static Border sectionBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12),
                new Color(0x4fc3f7));
    }

    /** Style a JTable to match dark theme */
    public static void styleTable(JTable table) {
        table.setBackground(new Color(0x0f3460));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(0x4fc3f7, true));
        table.getTableHeader().setBackground(ACCENT2);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
    }

    /** Show success dialog */
    public static void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success ✔", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Show error dialog */
    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error ✖", JOptionPane.ERROR_MESSAGE);
    }

    /** Gradient background panel */
    public static JPanel gradientPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), BG_PANEL);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }

    /** Style JComboBox */
    public static <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(new Color(0x0f3460));
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return cb;
    }
}