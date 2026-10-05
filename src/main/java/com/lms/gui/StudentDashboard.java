package com.lms.gui;

import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.User;
import com.lms.service.AssignmentService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * StudentDashboard — the main GUI window for Student users.
 *
 * Tabs:
 * 1. My Courses — enrolled courses with grades
 * 2. All Courses — browse and enroll in available courses
 * 3. Profile — student information
 *
 * Demonstrates:
 * - Swing JTabbedPane, JTable, JScrollPane
 * - Runtime Polymorphism: user.getDashboardTitle() returns student-specific
 * title
 * - Multithreading: connects to AssignmentService notification thread
 */
public class StudentDashboard extends JFrame {

    private final User student;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;

    // Colours
    private static final Color BG = new Color(24, 27, 38);
    private static final Color PANEL = new Color(32, 36, 50);
    private static final Color ACCENT = new Color(99, 179, 237);
    private static final Color FG = new Color(220, 225, 240);

    public StudentDashboard(User student) {
        this.student = student;
        this.courseService = new CourseService();
        this.enrollmentService = new EnrollmentService();
        this.assignmentService = new AssignmentService();
        initUI();
        startNotifications();
    }

    private void initUI() {
        // Polymorphism: getDashboardTitle() returns "Student Dashboard — <name>"
        setTitle(student.getDashboardTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        // ---- Header ----
        JPanel header = buildHeader();

        // ---- Tabbed Pane ----
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(PANEL);
        tabs.setForeground(FG);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("📚 My Courses", buildMyCoursesPanel());
        tabs.addTab("🔍 Browse Courses", buildAllCoursesPanel());
        tabs.addTab("👤 Profile", buildProfilePanel());

        // ---- Layout ----
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    // =====================================================================
    // Header
    // =====================================================================
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 23, 34));
        panel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("🎓 " + student.getDashboardTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT);

        JLabel role = new JLabel(student.getRoleDescription());
        role.setFont(new Font("SansSerif", Font.PLAIN, 12));
        role.setForeground(new Color(140, 150, 170));

        JButton logout = new JButton("Logout");
        styleButton(logout, new Color(200, 60, 60));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(20, 23, 34));
        left.add(title, BorderLayout.NORTH);
        left.add(role, BorderLayout.SOUTH);

        panel.add(left, BorderLayout.WEST);
        panel.add(logout, BorderLayout.EAST);
        return panel;
    }

    // =====================================================================
    // My Courses Tab
    // =====================================================================
    private JPanel buildMyCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = { "Course", "Teacher", "Enrolled On", "Grade" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = buildTable(model);
        loadEnrollments(model);

        JButton refreshBtn = new JButton("🔄 Refresh");
        styleButton(refreshBtn, ACCENT);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            loadEnrollments(model);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(PANEL);
        top.add(sectionLabel("My Enrolled Courses"));
        top.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadEnrollments(DefaultTableModel model) {
        List<Enrollment> list = enrollmentService.getStudentEnrollments(student.getUserId());
        for (Enrollment e : list) {
            model.addRow(new Object[] {
                    e.getCourseName(),
                    "(Teacher)",
                    e.getEnrollDate() != null ? e.getEnrollDate().toString() : "-",
                    e.getGrade() != null ? e.getGrade() : "N/A"
            });
        }
    }

    // =====================================================================
    // Browse Courses Tab
    // =====================================================================
    private JPanel buildAllCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = { "#", "Course", "Teacher", "Max Students", "Enrolled", "Action" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5;
            }
        };

        JTable table = buildTable(model);
        loadAllCourses(model);

        // Enroll button column
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), () -> {
            int row = table.getSelectedRow();
            if (row < 0)
                return;
            Object val = model.getValueAt(row, 0);
            if (val == null)
                return;
            try {
                int courseId = Integer.parseInt(val.toString().trim());
                enrollmentService.enrollStudent(student.getUserId(), courseId);
                JOptionPane.showMessageDialog(this, "Successfully enrolled!", "Enrolled",
                        JOptionPane.INFORMATION_MESSAGE);
                model.setRowCount(0);
                loadAllCourses(model);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }));

        JButton refreshBtn = new JButton("🔄 Refresh");
        styleButton(refreshBtn, ACCENT);
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            loadAllCourses(model);
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(PANEL);
        top.add(sectionLabel("Available Courses"));
        top.add(refreshBtn);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadAllCourses(DefaultTableModel model) {
        List<Course> courses = courseService.getAllCourses();
        for (Course c : courses) {
            int enrolled = courseService.getEnrollmentCount(c.getCourseId());
            boolean alreadyEnrolled = enrollmentService.isEnrolled(student.getUserId(), c.getCourseId());
            model.addRow(new Object[] {
                    c.getCourseId(),
                    c.getTitle(),
                    c.getTeacherName(),
                    c.getMaxStudents(),
                    enrolled,
                    alreadyEnrolled ? "Enrolled ✓" : "Enroll"
            });
        }
    }

    // =====================================================================
    // Profile Tab
    // =====================================================================
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[][] rows = {
                { "Name", student.getName() },
                { "Email", student.getEmail() },
                { "Role", "Student" },
                { "Info", student.getRoleDescription() }
        };

        int row = 0;
        for (String[] r : rows) {
            gbc.gridx = 0;
            gbc.gridy = row;
            JLabel key = new JLabel(r[0] + ":");
            key.setFont(new Font("SansSerif", Font.BOLD, 13));
            key.setForeground(ACCENT);
            panel.add(key, gbc);

            gbc.gridx = 1;
            JLabel val = new JLabel(r[1]);
            val.setFont(new Font("SansSerif", Font.PLAIN, 13));
            val.setForeground(FG);
            panel.add(val, gbc);
            row++;
        }

        return panel;
    }

    // =====================================================================
    // Multithreading — Notification Integration
    // =====================================================================
    /**
     * Starts the AssignmentService background thread.
     * The callback runs on the Event Dispatch Thread (EDT) for safe Swing updates.
     */
    private void startNotifications() {
        assignmentService.startNotificationThread(message -> SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(this, message, "Due Date Alert", JOptionPane.WARNING_MESSAGE)));
    }

    // =====================================================================
    // Shared Swing helpers
    // =====================================================================
    private JTable buildTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(new Color(24, 27, 38));
        table.setForeground(FG);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setBackground(new Color(40, 45, 60));
        table.getTableHeader().setForeground(ACCENT);
        table.setGridColor(new Color(50, 55, 70));
        table.setSelectionBackground(new Color(55, 65, 100));
        return table;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(ACCENT);
        lbl.setBorder(new EmptyBorder(0, 0, 0, 15));
        return lbl;
    }
}
