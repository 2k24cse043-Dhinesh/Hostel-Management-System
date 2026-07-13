package hostel.student;

import hostel.db.DBConnection;
import hostel.utils.LoginChoiceFrame;
import hostel.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Student Dashboard — Details, Fee Payment, Leave Request, Query/Feedback.
 */
public class StudentDashboard extends JFrame {

    private final int    studentId;
    private final String studentName;

    public StudentDashboard(int studentId, String studentName) {
        this.studentId   = studentId;
        this.studentName = studentName;

        setTitle("Student Dashboard — " + studentName);
        setSize(820, 580);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = UIHelper.gradientPanel();
        main.setLayout(new BorderLayout());

        // ── Top Bar ──────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(0x0f3460));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("🎓  Welcome, " + studentName + "  |  Student Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton logoutBtn = UIHelper.dangerButton("Logout");
        logoutBtn.addActionListener(e -> { new LoginChoiceFrame().setVisible(true); dispose(); });

        topBar.add(title,     BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // ── Tabs ─────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIHelper.BG_PANEL);
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("👤 My Details",     buildDetailsTab());
        tabs.addTab("💰 Fee Payment",    buildFeeTab());
        tabs.addTab("🚪 Leave Request",  buildLeaveTab());
        tabs.addTab("💬 Query/Feedback", buildQueryTab());

        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        add(main);
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 1 — Student Details
    // ─────────────────────────────────────────────────────────
    private JPanel buildDetailsTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(UIHelper.sectionBorder("  My Profile  "));

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT s.*, r.room_number, r.room_type, r.monthly_fee " +
                "FROM students s LEFT JOIN rooms r ON s.room_id=r.room_id WHERE s.student_id=?");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                addDetail(infoPanel, "Student ID:",  String.valueOf(rs.getInt("student_id")));
                addDetail(infoPanel, "Full Name:",   rs.getString("name"));
                addDetail(infoPanel, "Email:",       rs.getString("email"));
                addDetail(infoPanel, "Phone:",       rs.getString("phone"));
                addDetail(infoPanel, "Course:",      rs.getString("course"));
                addDetail(infoPanel, "Year:",        String.valueOf(rs.getInt("year")));
                addDetail(infoPanel, "Room:",        rs.getString("room_number") != null
                    ? "Room " + rs.getString("room_number") + " (" + rs.getString("room_type") + ")"
                    : "Not Assigned");
                addDetail(infoPanel, "Monthly Fee:", rs.getString("monthly_fee") != null
                    ? "₹" + rs.getDouble("monthly_fee") : "N/A");
                addDetail(infoPanel, "Status:",      rs.getString("status"));
                addDetail(infoPanel, "Join Date:",   String.valueOf(rs.getDate("join_date")));
            }
        } catch (SQLException ex) {
            UIHelper.showError(this, ex.getMessage());
        }

        panel.add(infoPanel, BorderLayout.NORTH);
        return panel;
    }

    private void addDetail(JPanel p, String label, String value) {
        JLabel lbl = UIHelper.styledLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        JLabel val = new JLabel(value != null ? value : "—");
        val.setForeground(Color.WHITE);
        val.setFont(new Font("SansSerif", Font.PLAIN, 13));
        p.add(lbl);
        p.add(val);
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 2 — Fee Payment (Student pays here)
    // ─────────────────────────────────────────────────────────
    private JPanel buildFeeTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // ── Fee Summary Info Grid ──
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 14));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(UIHelper.sectionBorder("  My Annual Fee Summary  "));

        // ── Progress Bar ──
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("SansSerif", Font.BOLD, 12));
        progressBar.setPreferredSize(new Dimension(400, 28));

        JPanel progressPanel = new JPanel(new BorderLayout(0, 6));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(UIHelper.sectionBorder("  Payment Progress  "));

        JLabel progressNote = new JLabel("", SwingConstants.CENTER);
        progressNote.setFont(new Font("SansSerif", Font.PLAIN, 12));

        progressPanel.add(progressBar,  BorderLayout.CENTER);
        progressPanel.add(progressNote, BorderLayout.SOUTH);

        // ── Pay Now Form ──
        JPanel payForm = new JPanel(new GridLayout(2, 3, 10, 8));
        payForm.setOpaque(false);
        payForm.setBorder(UIHelper.sectionBorder("  Pay Fee  "));

        JTextField amountField = UIHelper.styledField(10);
        JButton payBtn         = UIHelper.primaryButton("Pay Now");
        JButton refreshBtn     = UIHelper.secondaryButton("🔄 Refresh");

        payForm.add(UIHelper.styledLabel("Amount to Pay (₹):"));
        payForm.add(new JLabel()); // spacer
        payForm.add(new JLabel()); // spacer
        payForm.add(amountField);
        payForm.add(payBtn);
        payForm.add(refreshBtn);

        // ── Load fee data ──
        loadFeeData(infoPanel, progressBar, progressNote, amountField, payBtn, refreshBtn);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 16));
        center.setOpaque(false);
        center.add(infoPanel);
        center.add(progressPanel);

        panel.add(center,  BorderLayout.CENTER);
        panel.add(payForm, BorderLayout.SOUTH);
        return panel;
    }

    private void loadFeeData(JPanel infoPanel, JProgressBar progressBar,
                              JLabel progressNote, JTextField amountField,
                              JButton payBtn, JButton refreshBtn) {
        infoPanel.removeAll();
        try {
            Connection con = DBConnection.getConnection();

            // ── STEP 1: Sync total_fee from the student's actual room ─────────
            // Always recalculate to fix wrong/default values set at registration.
            PreparedStatement syncPs = con.prepareStatement(
                "UPDATE fee_payments fp " +
                "JOIN students s ON fp.student_id = s.student_id " +
                "JOIN rooms r ON s.room_id = r.room_id " +
                "SET fp.total_fee = r.monthly_fee * 12 " +
                "WHERE fp.student_id = ? AND s.room_id IS NOT NULL");
            syncPs.setInt(1, studentId);
            syncPs.executeUpdate();
            // ──────────────────────────────────────────────────────────────────

            PreparedStatement ps = con.prepareStatement(
                "SELECT total_fee, paid_amount, " +
                "(total_fee - paid_amount) AS pending, year, last_paid_date " +
                "FROM fee_payments WHERE student_id=? ORDER BY year DESC LIMIT 1");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double totalFee = rs.getDouble("total_fee");
                double paid     = rs.getDouble("paid_amount");
                double pending  = rs.getDouble("pending");
                int    year     = rs.getInt("year");
                String lastPaid = rs.getString("last_paid_date") != null
                                ? rs.getString("last_paid_date") : "Not yet paid";

                addDetail(infoPanel, "Academic Year:",    String.valueOf(year));
                addDetail(infoPanel, "Total Annual Fee:", "₹" + String.format("%.2f", totalFee));
                addDetail(infoPanel, "Amount Paid:",      "₹" + String.format("%.2f", paid));

                // Pending in red/green
                JLabel pendingLbl = UIHelper.styledLabel("Pending Amount:");
                pendingLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
                JLabel pendingVal = new JLabel("₹" + String.format("%.2f", pending));
                pendingVal.setFont(new Font("SansSerif", Font.BOLD, 14));
                pendingVal.setForeground(pending == 0 ? UIHelper.SUCCESS : UIHelper.DANGER);
                infoPanel.add(pendingLbl);
                infoPanel.add(pendingVal);

                addDetail(infoPanel, "Last Payment Date:", lastPaid);

                // Progress bar
                int pct = totalFee > 0 ? (int) ((paid / totalFee) * 100) : 0;
                progressBar.setValue(pct);
                progressBar.setString(pct + "% Paid");
                progressBar.setForeground(pct == 100 ? UIHelper.SUCCESS : UIHelper.ACCENT);
                progressBar.setBackground(new Color(0x0f3460));

                if (pct == 100) {
                    progressNote.setText("✅ Fee fully paid for " + year + "!");
                    progressNote.setForeground(UIHelper.SUCCESS);
                    payBtn.setEnabled(false);
                    amountField.setEnabled(false);
                } else if (totalFee == 0) {
                    progressBar.setString("Room not assigned yet");
                    progressNote.setText("ℹ  Fee will be calculated once admin assigns your room.");
                    progressNote.setForeground(UIHelper.INFO);
                    payBtn.setEnabled(false);
                    amountField.setEnabled(false);
                } else {
                    progressNote.setText("⚠  ₹" + String.format("%.2f", pending) + " still pending for " + year);
                    progressNote.setForeground(UIHelper.WARNING);
                    payBtn.setEnabled(true);
                    amountField.setEnabled(true);
                }

                // Pay button action
                payBtn.addActionListener(e -> {
                    String amtText = amountField.getText().trim();
                    if (amtText.isEmpty()) {
                        UIHelper.showError(this, "Please enter an amount.");
                        return;
                    }
                    double amount;
                    try {
                        amount = Double.parseDouble(amtText);
                    } catch (NumberFormatException nfe) {
                        UIHelper.showError(this, "Enter a valid numeric amount.");
                        return;
                    }
                    if (amount <= 0) {
                        UIHelper.showError(this, "Amount must be greater than 0.");
                        return;
                    }

                    // Fetch fresh pending from DB to avoid stale UI data
                    try {
                        Connection c2 = DBConnection.getConnection();
                        PreparedStatement checkPs = c2.prepareStatement(
                            "SELECT total_fee, paid_amount, year FROM fee_payments " +
                            "WHERE student_id=? ORDER BY year DESC LIMIT 1");
                        checkPs.setInt(1, studentId);
                        ResultSet cr = checkPs.executeQuery();
                        if (cr.next()) {
                            double latestPending = cr.getDouble("total_fee") - cr.getDouble("paid_amount");
                            int    feeYear       = cr.getInt("year");
                            if (amount > latestPending) {
                                UIHelper.showError(this,
                                    "Amount exceeds pending fee!\nPending: ₹" +
                                    String.format("%.2f", latestPending));
                                return;
                            }
                            PreparedStatement upd = c2.prepareStatement(
                                "UPDATE fee_payments " +
                                "SET paid_amount = paid_amount + ?, last_paid_date = CURDATE() " +
                                "WHERE student_id=? AND year=?");
                            upd.setDouble(1, amount);
                            upd.setInt(2, studentId);
                            upd.setInt(3, feeYear);
                            upd.executeUpdate();
                            UIHelper.showSuccess(this,
                                "₹" + String.format("%.2f", amount) + " paid successfully!");
                            amountField.setText("");
                            // Reload the tab
                            loadFeeData(infoPanel, progressBar, progressNote,
                                        amountField, payBtn, refreshBtn);
                            infoPanel.revalidate();
                            infoPanel.repaint();
                        }
                    } catch (SQLException ex) {
                        UIHelper.showError(this, "DB Error: " + ex.getMessage());
                    }
                });

                refreshBtn.addActionListener(e -> {
                    loadFeeData(infoPanel, progressBar, progressNote, amountField, payBtn, refreshBtn);
                    infoPanel.revalidate();
                    infoPanel.repaint();
                });

            } else {
                // ── No fee record found — auto-create one based on assigned room ──
                try {
                    PreparedStatement roomPs = con.prepareStatement(
                        "SELECT r.monthly_fee FROM students s " +
                        "LEFT JOIN rooms r ON s.room_id = r.room_id " +
                        "WHERE s.student_id = ?");
                    roomPs.setInt(1, studentId);
                    ResultSet roomRs = roomPs.executeQuery();

                    // Use actual room fee; if no room assigned yet, total_fee stays 0
                    // and will be corrected automatically when admin assigns a room.
                    double monthlyFee = 0.00;
                    if (roomRs.next() && roomRs.getString("monthly_fee") != null) {
                        monthlyFee = roomRs.getDouble("monthly_fee");
                    }
                    double annualFee   = monthlyFee * 12;
                    int    currentYear = java.util.Calendar.getInstance()
                                            .get(java.util.Calendar.YEAR);

                    PreparedStatement insertFee = con.prepareStatement(
                        "INSERT INTO fee_payments (student_id, total_fee, paid_amount, year) " +
                        "VALUES (?, ?, 0.00, ?)");
                    insertFee.setInt(1, studentId);
                    insertFee.setDouble(2, annualFee);
                    insertFee.setInt(3, currentYear);
                    insertFee.executeUpdate();

                    // Reload now that the record exists
                    loadFeeData(infoPanel, progressBar, progressNote,
                                amountField, payBtn, refreshBtn);
                    return;

                } catch (SQLException createEx) {
                    addDetail(infoPanel, "Status:", "Fee setup failed: " + createEx.getMessage());
                }

                progressBar.setValue(0);
                progressBar.setString("No Data");
                progressNote.setText("Could not set up fee record. Contact admin.");
                progressNote.setForeground(UIHelper.WARNING);
                payBtn.setEnabled(false);
                amountField.setEnabled(false);
            }

        } catch (SQLException ex) {
            UIHelper.showError(this, "DB Error: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 3 — Leave Request
    // ─────────────────────────────────────────────────────────
    private JPanel buildLeaveTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "From", "To", "Reason", "Status", "Applied On"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadLeaveHistory(model);

        JPanel applyForm = new JPanel(new GridLayout(4, 2, 10, 8));
        applyForm.setOpaque(false);
        applyForm.setBorder(UIHelper.sectionBorder("  Apply for Leave  "));

        JTextField fromField   = UIHelper.styledField(12);
        JTextField toField     = UIHelper.styledField(12);
        JTextField reasonField = UIHelper.styledField(30);
        fromField.setToolTipText("Format: YYYY-MM-DD");
        toField.setToolTipText("Format: YYYY-MM-DD");

        applyForm.add(UIHelper.styledLabel("From Date (YYYY-MM-DD):"));
        applyForm.add(fromField);
        applyForm.add(UIHelper.styledLabel("To Date (YYYY-MM-DD):"));
        applyForm.add(toField);
        applyForm.add(UIHelper.styledLabel("Reason:"));
        applyForm.add(reasonField);

        JButton applyBtn = UIHelper.primaryButton("Apply Leave");
        JButton refBtn   = UIHelper.secondaryButton("🔄 Refresh");
        applyForm.add(applyBtn);
        applyForm.add(refBtn);

        applyBtn.addActionListener(e -> {
            String from   = fromField.getText().trim();
            String to     = toField.getText().trim();
            String reason = reasonField.getText().trim();
            if (from.isEmpty() || to.isEmpty() || reason.isEmpty()) {
                UIHelper.showError(this, "All fields are required.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO leave_requests (student_id,from_date,to_date,reason) VALUES (?,?,?,?)");
                ps.setInt(1, studentId);
                ps.setDate(2, java.sql.Date.valueOf(from));
                ps.setDate(3, java.sql.Date.valueOf(to));
                ps.setString(4, reason);
                ps.executeUpdate();
                UIHelper.showSuccess(this, "Leave request submitted successfully!");
                model.setRowCount(0);
                loadLeaveHistory(model);
                fromField.setText(""); toField.setText(""); reasonField.setText("");
            } catch (IllegalArgumentException iae) {
                UIHelper.showError(this, "Invalid date format. Use YYYY-MM-DD.");
            } catch (SQLException ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        });

        refBtn.addActionListener(e -> { model.setRowCount(0); loadLeaveHistory(model); });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(applyForm,              BorderLayout.SOUTH);
        return panel;
    }

    private void loadLeaveHistory(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT leave_id,from_date,to_date,reason,status,applied_on " +
                "FROM leave_requests WHERE student_id=? ORDER BY leave_id DESC");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("leave_id"),    rs.getDate("from_date"),
                    rs.getDate("to_date"),    rs.getString("reason"),
                    rs.getString("status"),   rs.getTimestamp("applied_on")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 4 — Query / Feedback
    // ─────────────────────────────────────────────────────────
    private JPanel buildQueryTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Subject", "Message", "Reply", "Status", "Submitted"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadQueries(model);

        JPanel submitForm = new JPanel(new GridLayout(3, 2, 10, 8));
        submitForm.setOpaque(false);
        submitForm.setBorder(UIHelper.sectionBorder("  Submit New Query / Feedback  "));

        JTextField subjectField = UIHelper.styledField(30);
        JTextField messageField = UIHelper.styledField(30);

        submitForm.add(UIHelper.styledLabel("Subject:"));
        submitForm.add(subjectField);
        submitForm.add(UIHelper.styledLabel("Message:"));
        submitForm.add(messageField);

        JButton submitBtn = UIHelper.primaryButton("Submit");
        JButton refBtn    = UIHelper.secondaryButton("🔄 Refresh");
        submitForm.add(submitBtn);
        submitForm.add(refBtn);

        submitBtn.addActionListener(e -> {
            String subject = subjectField.getText().trim();
            String message = messageField.getText().trim();
            if (subject.isEmpty() || message.isEmpty()) {
                UIHelper.showError(this, "Subject and message are required.");
                return;
            }
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO queries (student_id,subject,message) VALUES (?,?,?)");
                ps.setInt(1, studentId);
                ps.setString(2, subject);
                ps.setString(3, message);
                ps.executeUpdate();
                UIHelper.showSuccess(this, "Query submitted! Admin will reply soon.");
                model.setRowCount(0);
                loadQueries(model);
                subjectField.setText(""); messageField.setText("");
            } catch (SQLException ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        });

        refBtn.addActionListener(e -> { model.setRowCount(0); loadQueries(model); });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(submitForm,             BorderLayout.SOUTH);
        return panel;
    }

    private void loadQueries(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT query_id,subject,message,reply,status,submitted_on " +
                "FROM queries WHERE student_id=? ORDER BY query_id DESC");
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("query_id"),     rs.getString("subject"),
                    rs.getString("message"),
                    rs.getString("reply") != null ? rs.getString("reply") : "Awaiting reply...",
                    rs.getString("status"),    rs.getTimestamp("submitted_on")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }
}