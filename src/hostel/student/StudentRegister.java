package hostel.student;

import hostel.db.DBConnection;
import hostel.utils.LoginChoiceFrame;
import hostel.utils.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Student Registration Screen — Fullscreen with centred card layout.
 */
public class StudentRegister extends JFrame {

    private JTextField     nameField, emailField, phoneField, courseField;
    private JPasswordField passField, confirmPassField;
    private JComboBox<String> yearCombo;

    public StudentRegister() {
        setTitle("Student Registration — Hostel Management");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── FIX: Open maximized (fullscreen windowed) ─────────
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(500, 600));
        setResizable(true);
        setLocationRelativeTo(null);

        // ── Outer gradient fills entire screen ────────────────
        JPanel outer = UIHelper.gradientPanel();
        outer.setLayout(new GridBagLayout());   // centres the card

        // ── Card panel (dark bordered box in centre) ──────────
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(new Color(0x16213e));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x4fc3f7), 1),
            BorderFactory.createEmptyBorder(10, 10, 20, 10)
        ));
        card.setPreferredSize(new Dimension(480, 620));
        card.setMaximumSize(new Dimension(480, 620));

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(25, 20, 10, 20));

        JLabel title = UIHelper.headerLabel("🎓  Student Registration");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel sub = new JLabel("Fill in your details to register", SwingConstants.CENTER);
        sub.setForeground(UIHelper.TEXT_MUTED);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));

        header.add(title);
        header.add(sub);

        // ── Form ──────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(5, 50, 10, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.insets  = new Insets(4, 0, 4, 0);
        gbc.gridx   = 0;
        gbc.weightx = 1.0;   // fields stretch to fill column width

        nameField        = UIHelper.styledField(35);
        emailField       = UIHelper.styledField(35);
        passField        = UIHelper.styledPassword(35);
        confirmPassField = UIHelper.styledPassword(35);
        phoneField       = UIHelper.styledField(35);
        courseField      = UIHelper.styledField(35);
        yearCombo        = UIHelper.styledCombo(new String[]{"1", "2", "3", "4", "5"});

        // Ensure fields are never too narrow to show typed text
        Dimension fieldSize = new Dimension(340, 36);
        nameField.setPreferredSize(fieldSize);
        emailField.setPreferredSize(fieldSize);
        passField.setPreferredSize(fieldSize);
        confirmPassField.setPreferredSize(fieldSize);
        phoneField.setPreferredSize(fieldSize);
        courseField.setPreferredSize(fieldSize);
        yearCombo.setPreferredSize(fieldSize);

        Object[][] fields = {
            {"Full Name:",    nameField},
            {"Email:",        emailField},
            {"Password:",     passField},
            {"Confirm Pass:", confirmPassField},
            {"Phone:",        phoneField},
            {"Course:",       courseField},
            {"Year:",         yearCombo}
        };

        int row = 0;
        for (Object[] f : fields) {
            gbc.gridy  = row++; gbc.insets = new Insets(4, 0, 1, 0);
            form.add(UIHelper.styledLabel((String) f[0]), gbc);
            gbc.gridy  = row++; gbc.insets = new Insets(1, 0, 4, 0);
            form.add((Component) f[1], gbc);
        }

        JButton registerBtn = UIHelper.primaryButton("REGISTER");
        JButton backBtn     = UIHelper.secondaryButton("← Back to Login");

        gbc.gridy  = row++; gbc.insets = new Insets(16, 0, 6, 0);
        form.add(registerBtn, gbc);
        gbc.gridy  = row;   gbc.insets = new Insets(4, 0, 4, 0);
        form.add(backBtn, gbc);

        registerBtn.addActionListener(e -> register());
        backBtn.addActionListener(e -> {
            new LoginChoiceFrame().setVisible(true);
            dispose();
        });

        // Scroll pane in case screen height is very small
        JScrollPane scroll = new JScrollPane(form);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        // Centre the card inside the fullscreen outer panel
        outer.add(card, new GridBagConstraints());

        add(outer);
    }

    private void register() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String pass    = new String(passField.getPassword()).trim();
        String confirm = new String(confirmPassField.getPassword()).trim();
        String phone   = phoneField.getText().trim();
        String course  = courseField.getText().trim();
        int    year    = Integer.parseInt((String) yearCombo.getSelectedItem());

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || course.isEmpty()) {
            UIHelper.showError(this, "All fields are required.");
            return;
        }
        if (!pass.equals(confirm)) {
            UIHelper.showError(this, "Passwords do not match.");
            return;
        }
        if (!email.contains("@")) {
            UIHelper.showError(this, "Enter a valid email address.");
            return;
        }
        if (!phone.matches("[6-9][0-9]{9}")) {
            UIHelper.showError(this, "Phone number must be exactly 10 digits\nand must start with 6, 7, 8, or 9.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO students (name,email,password,phone,course,year,status) VALUES (?,?,?,?,?,?,'Pending')",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, pass);
            ps.setString(4, phone);
            ps.setString(5, course);
            ps.setInt(6, year);
            ps.executeUpdate();

            // ── AUTO-CREATE fee_payments record for the new student ──
            // total_fee is 0.00 here because no room is assigned at registration time.
            // StudentDashboard will auto-update total_fee = monthly_fee × 12
            // once admin approves the student and assigns a room.
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newStudentId = keys.getInt(1);
                int currentYear  = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);

                PreparedStatement feePs = con.prepareStatement(
                    "INSERT INTO fee_payments (student_id, total_fee, paid_amount, year) " +
                    "VALUES (?, 0.00, 0.00, ?)");
                feePs.setInt(1, newStudentId);
                feePs.setInt(2, currentYear);
                feePs.executeUpdate();
            }

            UIHelper.showSuccess(this,
                "Registration successful!\nYour account is pending admin approval.\nPlease login after approval.");
            new StudentLogin().setVisible(true);
            dispose();
        } catch (SQLIntegrityConstraintViolationException ex) {
            UIHelper.showError(this, "This email is already registered.");
        } catch (SQLException ex) {
            UIHelper.showError(this, "Error: " + ex.getMessage());
        }
    }
}