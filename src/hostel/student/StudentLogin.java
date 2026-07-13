package hostel.student;

import hostel.db.DBConnection;
import hostel.utils.LoginChoiceFrame;
import hostel.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Student Login Screen — Fullscreen with centred card layout.
 * FIX: email field no longer clips long addresses.
 */
public class StudentLogin extends JFrame {

    private JTextField     emailField;
    private JPasswordField passField;

    public StudentLogin() {
        setTitle("Student Login — Hostel Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── FIX 1: Open maximized (fullscreen windowed) ───────
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(500, 550));
        setResizable(true);
        setLocationRelativeTo(null);

        // ── Outer gradient background fills entire screen ─────
        JPanel outer = UIHelper.gradientPanel();
        outer.setLayout(new GridBagLayout());   // centres the card

        // ── Card panel ────────────────────────────────────────
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(0x16213e));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)
        ));
        card.setPreferredSize(new Dimension(460, 480));
        card.setMaximumSize(new Dimension(460, 480));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));

        JLabel icon = new JLabel("🎓", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 48));

        JLabel title = UIHelper.headerLabel("Student Portal");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Login with your email", SwingConstants.CENTER);
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
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(6, 0, 6, 0);
        gbc.gridx   = 0;
        // FIX 2: weightx=1 makes fields stretch to fill the column
        gbc.weightx = 1.0;

        // FIX 3: Larger column count — email like "sneha@gmail.com"
        //         won't be clipped even at smaller window sizes
        emailField = UIHelper.styledField(35);
        passField  = UIHelper.styledPassword(35);

        // FIX 4: Minimum width so short card width never clips text
        emailField.setMinimumSize(new Dimension(300, 36));
        emailField.setPreferredSize(new Dimension(340, 36));
        passField.setMinimumSize(new Dimension(300, 36));
        passField.setPreferredSize(new Dimension(340, 36));

        gbc.gridy = 0; form.add(UIHelper.styledLabel("Email Address"), gbc);
        gbc.gridy = 1; form.add(emailField, gbc);
        gbc.gridy = 2; form.add(UIHelper.styledLabel("Password"), gbc);
        gbc.gridy = 3; form.add(passField, gbc);

        JButton loginBtn = UIHelper.primaryButton("LOGIN");
        JButton backBtn  = UIHelper.secondaryButton("← Back");

        JButton regBtn = new JButton("New? Register Here");
        regBtn.setBorderPainted(false);
        regBtn.setContentAreaFilled(false);
        regBtn.setForeground(UIHelper.INFO);
        regBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));

        gbc.gridy = 4; gbc.insets = new Insets(18, 0, 6, 0); form.add(loginBtn, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(4,  0, 4, 0); form.add(backBtn,  gbc);
        gbc.gridy = 6; gbc.insets = new Insets(2,  0, 4, 0); form.add(regBtn,   gbc);

        loginBtn.addActionListener(e -> login());
        backBtn.addActionListener(e -> { new LoginChoiceFrame().setVisible(true); dispose(); });
        regBtn.addActionListener(e  -> { new StudentRegister().setVisible(true);  dispose(); });

        getRootPane().setDefaultButton(loginBtn);

        card.add(header, BorderLayout.NORTH);
        card.add(form,   BorderLayout.CENTER);

        // Centre the card inside the fullscreen outer panel
        outer.add(card, new GridBagConstraints());

        add(outer);
    }

    private void login() {
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            UIHelper.showError(this, "Please enter email and password.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM students WHERE email=? AND password=?");
            ps.setString(1, email);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                if ("Pending".equals(status)) {
                    UIHelper.showError(this, "Your account is pending admin approval.");
                } else if ("Rejected".equals(status)) {
                    UIHelper.showError(this, "Your registration was rejected. Contact admin.");
                } else {
                    int    studentId = rs.getInt("student_id");
                    String name      = rs.getString("name");
                    UIHelper.showSuccess(this, "Welcome, " + name + "!");
                    new StudentDashboard(studentId, name).setVisible(true);
                    dispose();
                }
            } else {
                UIHelper.showError(this, "Invalid email or password.");
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, "DB Error: " + ex.getMessage());
        }
    }
}