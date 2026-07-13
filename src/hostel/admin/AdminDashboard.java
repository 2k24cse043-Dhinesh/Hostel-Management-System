package hostel.admin;

import hostel.db.DBConnection;
import hostel.utils.LoginChoiceFrame;
import hostel.utils.UIHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Admin Dashboard — manages students, rooms, leave requests, queries, and fee overview.
 */
public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("Admin Dashboard — Hostel Management");
        setSize(900, 620);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = UIHelper.gradientPanel();
        main.setLayout(new BorderLayout());

        // ── Top Bar ──────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIHelper.ACCENT2);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("🏠  ADMIN DASHBOARD");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton logoutBtn = UIHelper.dangerButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginChoiceFrame().setVisible(true);
            dispose();
        });

        topBar.add(title,     BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // ── Tabbed Pane ──────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(UIHelper.BG_PANEL);
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("📋 All Students",    buildAllStudentsTab());
        tabs.addTab("✅ Verify Students", buildVerifyTab());
        tabs.addTab("🏠 Room Allocation", buildRoomTab());
        tabs.addTab("🚪 Leave Requests",  buildLeaveTab());
        tabs.addTab("💬 Queries",         buildQueriesTab());
        tabs.addTab("💰 Fee Overview",    buildFeeTab());

        main.add(topBar, BorderLayout.NORTH);
        main.add(tabs,   BorderLayout.CENTER);
        add(main);
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 1 — All Students
    // ─────────────────────────────────────────────────────────
    private JPanel buildAllStudentsTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Email", "Phone", "Course", "Year", "Room", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadAllStudents(model);

        JButton refreshBtn = UIHelper.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> { model.setRowCount(0); loadAllStudents(model); });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAllStudents(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT s.student_id, s.name, s.email, s.phone, s.course, s.year, " +
                         "r.room_number, s.status FROM students s " +
                         "LEFT JOIN rooms r ON s.room_id = r.room_id ORDER BY s.student_id";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("student_id"),    rs.getString("name"),
                    rs.getString("email"),       rs.getString("phone"),
                    rs.getString("course"),      rs.getInt("year"),
                    rs.getString("room_number") != null ? rs.getString("room_number") : "Not Assigned",
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 2 — Verify Students
    // ─────────────────────────────────────────────────────────
    private JPanel buildVerifyTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Email", "Course", "Year", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadPendingStudents(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);

        JButton approveBtn = UIHelper.primaryButton("✅ Approve");
        JButton rejectBtn  = UIHelper.dangerButton("❌ Reject");
        JButton refreshBtn = UIHelper.secondaryButton("🔄 Refresh");

        approveBtn.addActionListener(e -> updateStudentStatus(table, model, "Approved"));
        rejectBtn.addActionListener(e  -> updateStudentStatus(table, model, "Rejected"));
        refreshBtn.addActionListener(e -> { model.setRowCount(0); loadPendingStudents(model); });

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadPendingStudents(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT student_id,name,email,course,year,status FROM students WHERE status='Pending'");
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("student_id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("course"),  rs.getInt("year"),    rs.getString("status")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    private void updateStudentStatus(JTable table, DefaultTableModel m, String status) {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a student first."); return; }
        int id = (int) m.getValueAt(row, 0);
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE students SET status=? WHERE student_id=?");
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Student " + status + " successfully!");
            m.setRowCount(0);
            loadPendingStudents(m);
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 3 — Room Allocation
    // ─────────────────────────────────────────────────────────
    private JPanel buildRoomTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] roomCols = {"Room No", "Type", "Floor", "Capacity", "Occupied", "Available", "Fee/Month"};
        DefaultTableModel roomModel = new DefaultTableModel(roomCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable roomTable = new JTable(roomModel);
        UIHelper.styleTable(roomTable);
        loadRooms(roomModel);

        JPanel assignPanel = new JPanel(new GridLayout(3, 2, 10, 8));
        assignPanel.setOpaque(false);
        assignPanel.setBorder(UIHelper.sectionBorder("  Assign Room to Student  "));

        JTextField studentIdField = UIHelper.styledField(10);
        JTextField roomIdField    = UIHelper.styledField(10);

        assignPanel.add(UIHelper.styledLabel("Student ID:"));
        assignPanel.add(studentIdField);
        assignPanel.add(UIHelper.styledLabel("Room ID (1-8):"));
        assignPanel.add(roomIdField);

        JButton assignBtn  = UIHelper.primaryButton("Assign Room");
        JButton refreshBtn = UIHelper.secondaryButton("🔄 Refresh");
        assignPanel.add(assignBtn);
        assignPanel.add(refreshBtn);

        assignBtn.addActionListener(e -> {
            try {
                int sid = Integer.parseInt(studentIdField.getText().trim());
                int rid = Integer.parseInt(roomIdField.getText().trim());
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE students SET room_id=? WHERE student_id=?");
                ps.setInt(1, rid);
                ps.setInt(2, sid);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    con.createStatement().executeUpdate(
                        "UPDATE rooms SET occupied = (SELECT COUNT(*) FROM students WHERE room_id=" + rid + ") WHERE room_id=" + rid);
                    UIHelper.showSuccess(this, "Room assigned successfully!");
                    roomModel.setRowCount(0);
                    loadRooms(roomModel);
                } else {
                    UIHelper.showError(this, "Student ID not found.");
                }
            } catch (NumberFormatException nfe) {
                UIHelper.showError(this, "Enter valid numeric IDs.");
            } catch (SQLException ex) {
                UIHelper.showError(this, ex.getMessage());
            }
        });

        refreshBtn.addActionListener(e -> { roomModel.setRowCount(0); loadRooms(roomModel); });

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(assignPanel, BorderLayout.CENTER);

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void loadRooms(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT room_id, room_number, room_type, floor_no, capacity, occupied, " +
                "(capacity-occupied) AS available, monthly_fee FROM rooms ORDER BY room_id");
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getString("room_number"), rs.getString("room_type"),
                    rs.getInt("floor_no"),        rs.getInt("capacity"),
                    rs.getInt("occupied"),         rs.getInt("available"),
                    "₹" + rs.getDouble("monthly_fee")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 4 — Leave Requests
    // ─────────────────────────────────────────────────────────
    private JPanel buildLeaveTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Leave ID", "Student ID", "Student Name", "From", "To", "Reason", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadLeaveRequests(model);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.setOpaque(false);

        JButton approveBtn = UIHelper.primaryButton("✅ Approve");
        JButton rejectBtn  = UIHelper.dangerButton("❌ Reject");
        JButton refreshBtn = UIHelper.secondaryButton("🔄 Refresh");

        approveBtn.addActionListener(e -> updateLeave(table, model, "Approved"));
        rejectBtn.addActionListener(e  -> updateLeave(table, model, "Rejected"));
        refreshBtn.addActionListener(e -> { model.setRowCount(0); loadLeaveRequests(model); });

        btnPanel.add(approveBtn);
        btnPanel.add(rejectBtn);
        btnPanel.add(refreshBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadLeaveRequests(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT l.leave_id, l.student_id, s.name, l.from_date, l.to_date, l.reason, l.status " +
                         "FROM leave_requests l JOIN students s ON l.student_id=s.student_id ORDER BY l.leave_id DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("leave_id"),    rs.getInt("student_id"),
                    rs.getString("name"),     rs.getDate("from_date"),
                    rs.getDate("to_date"),    rs.getString("reason"),
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    private void updateLeave(JTable table, DefaultTableModel m, String status) {
        int row = table.getSelectedRow();
        if (row < 0) { UIHelper.showError(this, "Select a leave request first."); return; }
        int id = (int) m.getValueAt(row, 0);
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "UPDATE leave_requests SET status=? WHERE leave_id=?");
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            UIHelper.showSuccess(this, "Leave request " + status + "!");
            m.setRowCount(0);
            loadLeaveRequests(m);
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 5 — Queries / Feedback
    // ─────────────────────────────────────────────────────────
    private JPanel buildQueriesTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Query ID", "Student", "Subject", "Message", "Reply", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);
        loadQueries(model);

        JPanel replyPanel = new JPanel(new BorderLayout(5, 5));
        replyPanel.setOpaque(false);
        replyPanel.setBorder(UIHelper.sectionBorder("  Reply to Selected Query  "));

        JTextField replyField = UIHelper.styledField(30);
        JButton replyBtn      = UIHelper.primaryButton("Send Reply");
        JButton resolveBtn    = UIHelper.secondaryButton("Mark Resolved");
        JButton refreshBtn    = UIHelper.secondaryButton("🔄 Refresh");

        JPanel replyBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        replyBtns.setOpaque(false);
        replyBtns.add(replyBtn);
        replyBtns.add(resolveBtn);
        replyBtns.add(refreshBtn);

        replyPanel.add(UIHelper.styledLabel("Reply Text:"), BorderLayout.NORTH);
        replyPanel.add(replyField,                          BorderLayout.CENTER);
        replyPanel.add(replyBtns,                           BorderLayout.SOUTH);

        replyBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this, "Select a query first."); return; }
            int id = (int) model.getValueAt(row, 0);
            String reply = replyField.getText().trim();
            if (reply.isEmpty()) { UIHelper.showError(this, "Reply cannot be empty."); return; }
            try {
                PreparedStatement ps = DBConnection.getConnection()
                    .prepareStatement("UPDATE queries SET reply=?, status='Resolved' WHERE query_id=?");
                ps.setString(1, reply);
                ps.setInt(2, id);
                ps.executeUpdate();
                UIHelper.showSuccess(this, "Reply sent!");
                model.setRowCount(0);
                loadQueries(model);
                replyField.setText("");
            } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
        });

        resolveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { UIHelper.showError(this, "Select a query first."); return; }
            int id = (int) model.getValueAt(row, 0);
            try {
                PreparedStatement ps = DBConnection.getConnection()
                    .prepareStatement("UPDATE queries SET status='Resolved' WHERE query_id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                UIHelper.showSuccess(this, "Marked as Resolved!");
                model.setRowCount(0);
                loadQueries(model);
            } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
        });

        refreshBtn.addActionListener(e -> { model.setRowCount(0); loadQueries(model); });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(replyPanel,             BorderLayout.SOUTH);
        return panel;
    }

    private void loadQueries(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT q.query_id, s.name, q.subject, q.message, q.reply, q.status " +
                         "FROM queries q JOIN students s ON q.student_id=s.student_id ORDER BY q.query_id DESC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("query_id"),   rs.getString("name"),
                    rs.getString("subject"), rs.getString("message"),
                    rs.getString("reply") != null ? rs.getString("reply") : "No reply yet",
                    rs.getString("status")
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    // ─────────────────────────────────────────────────────────
    //  TAB 6 — Fee Overview (VIEW ONLY — Admin cannot pay)
    // ─────────────────────────────────────────────────────────
    private JPanel buildFeeTab() {
        JPanel panel = UIHelper.darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Columns ──
        String[] cols = {"ID", "Student Name", "Course", "Year", "Total Fee (₹)", "Paid (₹)", "Pending (₹)", "Last Paid"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UIHelper.styleTable(table);

        // ── Color-code rows: green = fully paid, red = pending ──
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    try {
                        double pending = Double.parseDouble(
                            t.getValueAt(row, 6).toString().replace(",", ""));
                        setBackground(pending == 0
                            ? new Color(0x1b5e20)   // fully paid — dark green
                            : new Color(0x7f0000)); // pending    — dark red
                    } catch (NumberFormatException e) {
                        setBackground(UIHelper.BG_PANEL);
                    }
                    setForeground(Color.WHITE);
                }
                return this;
            }
        });

        loadFeeOverview(model);

        // ── Summary bar ──
        JLabel summaryLabel = buildFeeSummaryLabel(model);

        // ── Refresh button only — Admin cannot pay ──
        JPanel southPanel = new JPanel(new BorderLayout(0, 6));
        southPanel.setOpaque(false);

        JLabel noteLabel = new JLabel("  ℹ  Fee payments can only be made by students from their portal.", SwingConstants.LEFT);
        noteLabel.setForeground(UIHelper.INFO);
        noteLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        noteLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        JButton refreshBtn = UIHelper.secondaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            loadFeeOverview(model);
            summaryLabel.setText(buildFeeSummaryText(model));
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.setOpaque(false);
        btnRow.add(refreshBtn);

        southPanel.add(summaryLabel, BorderLayout.NORTH);
        southPanel.add(noteLabel,    BorderLayout.CENTER);
        southPanel.add(btnRow,       BorderLayout.SOUTH);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(southPanel,             BorderLayout.SOUTH);
        return panel;
    }

    private void loadFeeOverview(DefaultTableModel m) {
        try {
            Connection con = DBConnection.getConnection();
            String sql =
                "SELECT s.student_id, s.name, s.course, " +
                "fp.total_fee, fp.paid_amount, " +
                "(fp.total_fee - fp.paid_amount) AS pending, " +
                "fp.year, fp.last_paid_date " +
                "FROM students s " +
                "JOIN fee_payments fp ON s.student_id = fp.student_id " +
                "ORDER BY fp.year DESC, s.name ASC";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                m.addRow(new Object[]{
                    rs.getInt("student_id"),
                    rs.getString("name"),
                    rs.getString("course"),
                    rs.getInt("year"),
                    String.format("%.2f", rs.getDouble("total_fee")),
                    String.format("%.2f", rs.getDouble("paid_amount")),
                    String.format("%.2f", rs.getDouble("pending")),
                    rs.getString("last_paid_date") != null
                        ? rs.getString("last_paid_date") : "Not Paid"
                });
            }
        } catch (SQLException ex) { UIHelper.showError(this, ex.getMessage()); }
    }

    private String buildFeeSummaryText(DefaultTableModel model) {
        int total    = model.getRowCount();
        int fullPaid = 0, hasPending = 0;
        for (int i = 0; i < total; i++) {
            try {
                double pending = Double.parseDouble(model.getValueAt(i, 6).toString().replace(",", ""));
                if (pending == 0) fullPaid++; else hasPending++;
            } catch (NumberFormatException ignored) {}
        }
        return "  Total Students: " + total
             + "   |   ✅ Fully Paid: " + fullPaid
             + "   |   ⚠ Pending: " + hasPending;
    }

    private JLabel buildFeeSummaryLabel(DefaultTableModel model) {
        JLabel label = new JLabel(buildFeeSummaryText(model), SwingConstants.LEFT);
        label.setForeground(UIHelper.TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));
        return label;
    }
}