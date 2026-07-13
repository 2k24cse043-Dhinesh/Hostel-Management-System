package hostel.utils;

import hostel.admin.AdminLogin;
import hostel.student.StudentLogin;
import hostel.student.StudentRegister;

import javax.swing.*;
import java.awt.*;

/**
 * Landing screen — full screen, choose Admin or Student portal.
 */
public class LoginChoiceFrame extends JFrame {

    public LoginChoiceFrame() {
        setTitle("Hostel Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // ── FULL SCREEN SETUP ─────────────────────────────────
        setUndecorated(true);                         // remove title bar
        setExtendedState(JFrame.MAXIMIZED_BOTH);      // maximize to full screen
        setResizable(true);
        // ─────────────────────────────────────────────────────

        // Main panel with gradient background
        JPanel main = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0,
                        new Color(0x1a1a2e), getWidth(), getHeight(), new Color(0x16213e));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        main.setLayout(new BorderLayout());

        // ── ESC key to exit ───────────────────────────────────
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> {
                if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED &&
                    e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                return false;
            });

        // ── Header ──────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(80, 20, 40, 20));

        JLabel title = new JLabel("\uD83C\uDFE0  HOSTEL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 36));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Select your portal to continue", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(new Color(0xaaaacc));

        header.add(title);
        header.add(subtitle);

        // ── Center — Card Buttons ─────────────────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 60, 0));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(700, 280));

        JButton adminBtn   = makeCard("\uD83D\uDC64", "Admin",   "Manage students & rooms", new Color(0xe94560));
        JButton studentBtn = makeCard("\uD83C\uDF93", "Student", "View details & pay fees", new Color(0x0f3460));

        adminBtn.addActionListener(e -> {
            new AdminLogin().setVisible(true);
            dispose();
        });
        studentBtn.addActionListener(e -> {
            new StudentLogin().setVisible(true);
            dispose();
        });

        btnPanel.add(adminBtn);
        btnPanel.add(studentBtn);
        centerWrapper.add(btnPanel);

        // ── Footer — Register link ────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));

        JLabel regLabel = new JLabel("New Student? ");
        regLabel.setForeground(new Color(0xaaaacc));
        regLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JLabel regLink = new JLabel("Register Here");
        regLink.setForeground(new Color(0x4fc3f7));
        regLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regLink.setFont(new Font("SansSerif", Font.BOLD, 16));
        regLink.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new StudentRegister().setVisible(true);
                dispose();
            }
        });

        footer.add(regLabel);
        footer.add(regLink);

        main.add(header,        BorderLayout.NORTH);
        main.add(centerWrapper, BorderLayout.CENTER);
        main.add(footer,        BorderLayout.SOUTH);

        add(main);
    }

    private JButton makeCard(String icon, String title, String sub, Color accent) {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Accent top bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 8, 8, 8);

                // Icon
                g2.setFont(new Font("SansSerif", Font.PLAIN, 56));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2, getHeight() / 2 - 20);

                // Title
                g2.setFont(new Font("SansSerif", Font.BOLD, 24));
                fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, getHeight() / 2 + 30);

                // Subtitle
                g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
                fm = g2.getFontMetrics();
                g2.setColor(new Color(0xaaaacc));
                g2.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, getHeight() / 2 + 58);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border
            }
        };

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 40));
                btn.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(255, 255, 255, 20));
                btn.repaint();
            }
        });

        btn.setPreferredSize(new Dimension(280, 240));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}