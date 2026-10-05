package com.lms.gui;

import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.Lesson;
import com.lms.model.User;
import com.lms.service.AssignmentService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * TeacherDashboard — GUI for Teacher users.
 *
 * Tabs:
 * 1. My Courses — view/create/delete courses taught
 * 2. Manage Lessons — add/edit/delete lessons in a course
 * 3. Assignments — create assignments, grade students
 * 4. Students — view students enrolled in each course
 * 5. Profile — teacher information
 */
public class TeacherDashboard extends JFrame {

    private final User teacher;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;

    private static final Color BG = new Color(24, 27, 38);
    private static final Color PANEL = new Color(32, 36, 50);
    private static final Color ACCENT = new Color(130, 210, 130);
    private static final Color FG = new Color(220, 225, 240);

    public TeacherDashboard(User teacher) {
        this.teacher = teacher;
        this.courseService = new CourseService();
        this.lessonService = new LessonService();
        this.assignmentService = new AssignmentService();
        this.enrollmentService = new EnrollmentService();
        initUI();
        startNotifications();
    }

    private void initUI() {
        setTitle(teacher.getDashboardTitle());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);

        JPanel header = buildHeader();

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(PANEL);
        tabs.setForeground(FG);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("📚 My Courses", buildMyCourseTab());
        tabs.addTab("📖 Lessons", buildLessonsTab());
        tabs.addTab("📝 Assignments", buildAssignmentsTab());
        tabs.addTab("👥 Students", buildStudentsTab());
        tabs.addTab("👤 Profile", buildProfileTab());

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

        JLabel title = new JLabel("🧑‍🏫 " + teacher.getDashboardTitle());
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(ACCENT);

        JLabel role = new JLabel(teacher.getRoleDescription());
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
    // Tab 1: My Courses
    // =====================================================================
    private JPanel buildMyCourseTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] cols = { "ID", "Course Title", "Description", "Max Students", "Enrolled" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = buildTable(model);
        loadTeacherCourses(model);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(PANEL);
        toolbar.add(sectionLabel("My Courses"));

        JButton addBtn = new JButton("+ Create Course");
        styleButton(addBtn, ACCENT);
        addBtn.addActionListener(e -> showCreateCourseDialog(model));
        toolbar.add(addBtn);

        JButton editBtn = new JButton("✏ Edit");
        styleButton(editBtn, new Color(80, 130, 200));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select a course to edit.");
                return;
            }
            int courseId = Integer.parseInt(model.getValueAt(row, 0).toString());
            showEditCourseDialog(courseId, model);
        });
        toolbar.add(editBtn);

        JButton delBtn = new JButton("🗑 Delete");
        styleButton(delBtn, new Color(200, 60, 60));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select a course to delete.");
                return;
            }
            int courseId = Integer.parseInt(model.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete this course and all its lessons/assignments?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                courseService.deleteCourse(courseId);
                model.setRowCount(0);
                loadTeacherCourses(model);
            }
        });
        toolbar.add(delBtn);

        JButton refresh = new JButton("🔄");
        styleButton(refresh, new Color(60, 60, 80));
        refresh.addActionListener(e -> {
            model.setRowCount(0);
            loadTeacherCourses(model);
        });
        toolbar.add(refresh);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void loadTeacherCourses(DefaultTableModel model) {
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getUserId());
        for (Course c : courses) {
            int enrolled = courseService.getEnrollmentCount(c.getCourseId());
            model.addRow(new Object[] {
                    c.getCourseId(), c.getTitle(), c.getDescription(), c.getMaxStudents(), enrolled
            });
        }
    }

    private void showCreateCourseDialog(DefaultTableModel model) {
        JTextField titleF = new JTextField(20);
        JTextField descF = new JTextField(20);
        JTextField maxF = new JTextField("50", 5);

        JPanel p = new JPanel(new GridLayout(6, 1, 4, 4));
        p.add(new JLabel("Title:"));
        p.add(titleF);
        p.add(new JLabel("Description:"));
        p.add(descF);
        p.add(new JLabel("Max Students:"));
        p.add(maxF);

        int res = JOptionPane.showConfirmDialog(this, p, "Create Course", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int max = Integer.parseInt(maxF.getText().trim());
                courseService.createCourse(titleF.getText(), descF.getText(), teacher.getUserId(), max);
                model.setRowCount(0);
                loadTeacherCourses(model);
                JOptionPane.showMessageDialog(this, "Course created successfully!");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showEditCourseDialog(int courseId, DefaultTableModel model) {
        try {
            Course c = courseService.getCourseById(courseId);
            JTextField titleF = new JTextField(c.getTitle(), 20);
            JTextField descF = new JTextField(c.getDescription(), 20);
            JTextField maxF = new JTextField(String.valueOf(c.getMaxStudents()), 5);

            JPanel p = new JPanel(new GridLayout(6, 1, 4, 4));
            p.add(new JLabel("Title:"));
            p.add(titleF);
            p.add(new JLabel("Description:"));
            p.add(descF);
            p.add(new JLabel("Max Students:"));
            p.add(maxF);

            int res = JOptionPane.showConfirmDialog(this, p, "Edit Course", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                c.setTitle(titleF.getText().trim());
                c.setDescription(descF.getText().trim());
                c.setMaxStudents(Integer.parseInt(maxF.getText().trim()));
                courseService.updateCourse(c);
                model.setRowCount(0);
                loadTeacherCourses(model);
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    // =====================================================================
    // Tab 2: Lessons
    // =====================================================================
    private JPanel buildLessonsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Course picker
        JLabel courseLabel = new JLabel("Select Course:");
        courseLabel.setForeground(FG);
        courseLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        JComboBox<String> courseCombo = new JComboBox<>();
        courseCombo.setBackground(new Color(45, 50, 65));
        courseCombo.setForeground(FG);
        List<Course> courses = courseService.getCoursesByTeacher(teacher.getUserId());
        courses.forEach(c -> courseCombo.addItem(c.getCourseId() + ": " + c.getTitle()));

        // Lessons table
        String[] cols = { "#", "ID", "Title", "Order Index" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = buildTable(model);

        // Load on selection change
        courseCombo.addActionListener(e -> {
            int idx = courseCombo.getSelectedIndex();
            if (idx < 0 || courses.isEmpty())
                return;
            model.setRowCount(0);
            int courseId = courses.get(idx).getCourseId();
            List<Lesson> lessons = lessonService.getLessonsByCourse(courseId);
            int n = 1;
            for (Lesson l : lessons) {
                model.addRow(new Object[] { n++, l.getLessonId(), l.getTitle(), l.getOrderIndex() });
            }
        });
        if (courseCombo.getItemCount() > 0)
            courseCombo.setSelectedIndex(0);

        // Buttons
        JButton addBtn = new JButton("+ Add Lesson");
        styleButton(addBtn, ACCENT);
        addBtn.addActionListener(e -> {
            int idx = courseCombo.getSelectedIndex();
            if (idx < 0 || courses.isEmpty())
                return;
            int courseId = courses.get(idx).getCourseId();
            showAddLessonDialog(courseId, model, courses.get(idx));
        });

        JButton delBtn = new JButton("🗑 Delete");
        styleButton(delBtn, new Color(200, 60, 60));
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select a lesson to delete.");
                return;
            }
            int lessonId = Integer.parseInt(model.getValueAt(row, 1).toString());
            lessonService.deleteLesson(lessonId);
            model.removeRow(row);
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setBackground(PANEL);
        topBar.add(courseLabel);
        topBar.add(courseCombo);
        topBar.add(addBtn);
        topBar.add(delBtn);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showAddLessonDialog(int courseId, DefaultTableModel model, Course course) {
        JTextField titleF = new JTextField(20);
        JTextArea contentA = new JTextArea(5, 20);
        JTextField orderF = new JTextField("1", 5);

        JPanel p = new JPanel(new GridLayout(6, 1, 4, 4));
        p.add(new JLabel("Lesson Title:"));
        p.add(titleF);
        p.add(new JLabel("Content:"));
        p.add(new JScrollPane(contentA));
        p.add(new JLabel("Order Index:"));
        p.add(orderF);

        int res = JOptionPane.showConfirmDialog(this, p, "Add Lesson to: " + course.getTitle(),
                JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Lesson l = lessonService.addLesson(
                        courseId, titleF.getText(), contentA.getText(),
                        Integer.parseInt(orderF.getText().trim()));
                model.addRow(
                        new Object[] { model.getRowCount() + 1, l.getLessonId(), l.getTitle(), l.getOrderIndex() });
                JOptionPane.showMessageDialog(this, "Lesson added.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    // =====================================================================
    // Tab 3: Assignments
    // =====================================================================
    private JPanel buildAssignmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<Course> courses = courseService.getCoursesByTeacher(teacher.getUserId());

        JComboBox<String> courseCombo = new JComboBox<>();
        courses.forEach(c -> courseCombo.addItem(c.getCourseId() + ": " + c.getTitle()));

        String[] cols = { "ID", "Title", "Due Date", "Max Marks" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = buildTable(model);

        courseCombo.addActionListener(e -> {
            int idx = courseCombo.getSelectedIndex();
            if (idx < 0 || courses.isEmpty())
                return;
            model.setRowCount(0);
            List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courses.get(idx).getCourseId());
            assignments.forEach(a -> model.addRow(new Object[] {
                    a.getAssignmentId(), a.getTitle(),
                    a.getDueDate() != null ? a.getDueDate() : "-", a.getMaxMarks()
            }));
        });
        if (courseCombo.getItemCount() > 0)
            courseCombo.setSelectedIndex(0);

        JButton addBtn = new JButton("+ New Assignment");
        styleButton(addBtn, ACCENT);
        addBtn.addActionListener(e -> {
            int idx = courseCombo.getSelectedIndex();
            if (idx < 0 || courses.isEmpty())
                return;
            showAddAssignmentDialog(courses.get(idx).getCourseId(), model);
        });

        JButton gradeBtn = new JButton("✏ Grade Student");
        styleButton(gradeBtn, new Color(80, 130, 200));
        gradeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select an assignment to grade.");
                return;
            }
            int assignId = Integer.parseInt(model.getValueAt(row, 0).toString());
            int idx = courseCombo.getSelectedIndex();
            if (idx >= 0)
                showGradeDialog(assignId, courses.get(idx).getCourseId());
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setBackground(PANEL);
        topBar.add(new JLabel("Course:") {
            {
                setForeground(FG);
            }
        });
        topBar.add(courseCombo);
        topBar.add(addBtn);
        topBar.add(gradeBtn);

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void showAddAssignmentDialog(int courseId, DefaultTableModel model) {
        JTextField titleF = new JTextField(20);
        JTextField descF = new JTextField(20);
        JTextField dueDateF = new JTextField(LocalDate.now().plusDays(7).toString(), 12);
        JTextField maxMarksF = new JTextField("100", 5);

        JPanel p = new JPanel(new GridLayout(8, 1, 4, 4));
        p.add(new JLabel("Title:"));
        p.add(titleF);
        p.add(new JLabel("Description:"));
        p.add(descF);
        p.add(new JLabel("Due Date (YYYY-MM-DD):"));
        p.add(dueDateF);
        p.add(new JLabel("Max Marks:"));
        p.add(maxMarksF);

        int res = JOptionPane.showConfirmDialog(this, p, "New Assignment", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                Assignment a = assignmentService.createAssignment(
                        courseId, titleF.getText(), descF.getText(),
                        LocalDate.parse(dueDateF.getText().trim()),
                        Integer.parseInt(maxMarksF.getText().trim()));
                model.addRow(new Object[] { a.getAssignmentId(), a.getTitle(), a.getDueDate(), a.getMaxMarks() });
                JOptionPane.showMessageDialog(this, "Assignment created!");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void showGradeDialog(int assignmentId, int courseId) {
        List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courseId);
        if (enrollments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students enrolled.");
            return;
        }

        String[] students = enrollments.stream()
                .map(en -> en.getStudentId() + " — (ID: " + en.getStudentId() + ")")
                .toArray(String[]::new);

        JComboBox<String> studentCombo = new JComboBox<>(students);
        JTextField marksF = new JTextField("85", 5);

        JPanel p = new JPanel(new GridLayout(4, 1, 4, 4));
        p.add(new JLabel("Student:"));
        p.add(studentCombo);
        p.add(new JLabel("Marks Obtained:"));
        p.add(marksF);

        int res = JOptionPane.showConfirmDialog(this, p, "Grade Submission", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int idx = studentCombo.getSelectedIndex();
                int studentId = enrollments.get(idx).getStudentId();
                int marks = Integer.parseInt(marksF.getText().trim());
                assignmentService.gradeSubmission(assignmentId, studentId, marks);
                JOptionPane.showMessageDialog(this, "Grade saved!");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        }
    }

    // =====================================================================
    // Tab 4: Students
    // =====================================================================
    private JPanel buildStudentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        List<Course> courses = courseService.getCoursesByTeacher(teacher.getUserId());
        JComboBox<String> courseCombo = new JComboBox<>();
        courses.forEach(c -> courseCombo.addItem(c.getCourseId() + ": " + c.getTitle()));

        String[] cols = { "Enrollment ID", "Student ID", "Enrolled On", "Grade" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable table = buildTable(model);

        courseCombo.addActionListener(e -> {
            int idx = courseCombo.getSelectedIndex();
            if (idx < 0 || courses.isEmpty())
                return;
            model.setRowCount(0);
            List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(courses.get(idx).getCourseId());
            enrollments.forEach(en -> model.addRow(new Object[] {
                    en.getEnrollmentId(), en.getStudentId(),
                    en.getEnrollDate() != null ? en.getEnrollDate() : "-",
                    en.getGrade() != null ? en.getGrade() : "N/A"
            }));
        });
        if (courseCombo.getItemCount() > 0)
            courseCombo.setSelectedIndex(0);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topBar.setBackground(PANEL);
        topBar.add(new JLabel("Course:") {
            {
                setForeground(FG);
            }
        });
        topBar.add(courseCombo);
        topBar.add(sectionLabel("Enrolled Students"));

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // =====================================================================
    // Tab 5: Profile
    // =====================================================================
    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[][] rows = {
                { "Name", teacher.getName() },
                { "Email", teacher.getEmail() },
                { "Role", "Teacher" },
                { "Details", teacher.getRoleDescription() }
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
    // Multithreading: Notifications
    // =====================================================================
    private void startNotifications() {
        assignmentService.startNotificationThread(message -> SwingUtilities.invokeLater(
                () -> JOptionPane.showMessageDialog(this, message, "Due Date Alert", JOptionPane.WARNING_MESSAGE)));
    }

    // =====================================================================
    // Helpers
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
        lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
        return lbl;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
