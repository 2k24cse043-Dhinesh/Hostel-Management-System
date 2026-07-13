package hostel.admin;

import hostel.db.DBConnection;
import hostel.utils.LoginChoiceFrame;
import hostel.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Admin Login Screen — Fullscreen with centred card layout
 */
public class AdminLogin extends JFrame {

    private JTextField     userField;
    private JPasswordField passField;

    public AdminLogin() {
        setTitle("Admin Login — Hostel Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── FIX 1: Open maximized (fullscreen windowed) ───────
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(500, 500));
        setResizable(true);
        setLocationRelativeTo(null);

        // ── Outer gradient background fills entire screen ─────
        JPanel outer = UIHelper.gradientPanel();
        outer.setLayout(new GridBagLayout());   // centres the card

        // ── Card panel (white-ish dark card in centre) ────────
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(0x16213e));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)
        ));
        // Fixed card width; height wraps content
        card.setPreferredSize(new Dimension(440, 420));
        card.setMaximumSize(new Dimension(440, 420));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));

        JLabel icon = new JLabel("👤", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));

        JLabel title = UIHelper.headerLabel("Admin Portal");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Enter your credentials", SwingConstants.CENTER);
        sub.setForeground(UIHelper.TEXT_MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(icon);
        header.add(title);
        header.add(sub);

        // ── Form ──────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx  = 0;
        // FIX 2: weightx=1 makes the field stretch to column width
        gbc.weightx = 1.0;

        // FIX 3: Use wider column count so fields don't clip text
        userField = UIHelper.styledField(30);
        passField = UIHelper.styledPassword(30);

        gbc.gridy = 0; form.add(UIHelper.styledLabel("Username"), gbc);
        gbc.gridy = 1; form.add(userField, gbc);
        gbc.gridy = 2; form.add(UIHelper.styledLabel("Password"), gbc);
        gbc.gridy = 3; form.add(passField, gbc);

        JButton loginBtn = UIHelper.primaryButton("LOGIN");
        JButton backBtn  = UIHelper.secondaryButton("← Back");

        // Make buttons fill the column width
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        gbc.gridy  = 4; gbc.insets = new Insets(18, 0, 6, 0);
        form.add(loginBtn, gbc);
        gbc.gridy  = 5; gbc.insets = new Insets(4, 0, 4, 0);
        form.add(backBtn, gbc);

        loginBtn.addActionListener(e -> login());
        backBtn.addActionListener(e -> {
            new LoginChoiceFrame().setVisible(true);
            dispose();
        });

        getRootPane().setDefaultButton(loginBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(form,   BorderLayout.CENTER);

        // Centre the card inside the fullscreen outer panel
        outer.add(card, new GridBagConstraints());

        add(outer);
    }

    private void login() {
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            UIHelper.showError(this, "Please enter both username and password.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM admin WHERE username=? AND password=?");
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                UIHelper.showSuccess(this, "Welcome, " + user + "!");
                new AdminDashboard().setVisible(true);
                dispose();
            } else {
                UIHelper.showError(this, "Invalid username or password.");
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "DB Error: " + ex.getMessage());
        }
    }
}