package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.database.DatabaseConnection;
import com.lms.model.Assignment;
import com.lms.util.InputValidator;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AssignmentService — manages assignments and demonstrates MULTITHREADING.
 *
 * Multithreading Feature:
 * A ScheduledExecutorService runs a background task every 60 seconds that
 * scans all active assignments. If any assignment is due within 1 day,
 * it fires a notification callback (shown as a JOptionPane in the GUI).
 *
 * The list of watched assignments is a synchronized collection to prevent
 * ConcurrentModificationException when both the main thread and the
 * scheduler thread access it simultaneously.
 *
 * Synchronization: watchedAssignments is wrapped with
 * Collections.synchronizedList().
 */
public class AssignmentService {

    // Synchronization: thread-safe list shared between GUI thread and scheduler
    // thread
    private final List<Assignment> watchedAssignments = Collections.synchronizedList(new ArrayList<>());

    // Single-threaded scheduler for the notification background task
    private ScheduledExecutorService scheduler;

    // Callback invoked from the background thread to notify the GUI
    private Consumer<String> notificationCallback;

    public AssignmentService() {
    }

    // ---- Assignment CRUD (using JDBC directly for simplicity) ----

    private Connection getConn() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get DB connection", e);
        }
    }

    /**
     * Creates a new assignment in the database.
     */
    public Assignment createAssignment(int courseId, String title, String description,
            LocalDate dueDate, int maxMarks) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Assignment title cannot be empty.");
        if (dueDate == null)
            throw new IllegalArgumentException("Due date is required.");
        if (maxMarks <= 0)
            throw new IllegalArgumentException("Max marks must be positive.");

        String sql = "INSERT INTO assignments (course_id, title, description, due_date, max_marks) VALUES (?, ?, ?, ?, ?)";
        Assignment a = new Assignment();
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, InputValidator.sanitize(title));
            ps.setString(3, InputValidator.sanitize(description));
            ps.setDate(4, Date.valueOf(dueDate));
            ps.setInt(5, maxMarks);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                a.setAssignmentId(keys.getInt(1));
                a.setCourseId(courseId);
                a.setTitle(title);
                a.setDescription(description);
                a.setDueDate(dueDate);
                a.setMaxMarks(maxMarks);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating assignment: " + e.getMessage(), e);
        }

        // Add to the watched list for the notification thread
        watchedAssignments.add(a);
        return a;
    }

    /**
     * Returns all assignments for a course.
     */
    public List<Assignment> getAssignmentsByCourse(int courseId) {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT * FROM assignments WHERE course_id = ? ORDER BY due_date";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Assignment a = new Assignment();
                a.setAssignmentId(rs.getInt("assignment_id"));
                a.setCourseId(rs.getInt("course_id"));
                a.setTitle(rs.getString("title"));
                a.setDescription(rs.getString("description"));
                Date d = rs.getDate("due_date");
                a.setDueDate(d != null ? d.toLocalDate() : null);
                a.setMaxMarks(rs.getInt("max_marks"));
                list.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching assignments: " + e.getMessage(), e);
        }
        // Keep watched list up-to-date
        watchedAssignments.clear();
        watchedAssignments.addAll(list);
        return list;
    }

    /**
     * Saves a student's grade for an assignment.
     */
    public void gradeSubmission(int assignmentId, int studentId, int marks) {
        String sql = "INSERT INTO submissions (assignment_id, student_id, marks_obtained) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE marks_obtained = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            ps.setInt(3, marks);
            ps.setInt(4, marks);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving grade: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an assignment.
     */
    public void deleteAssignment(int assignmentId) {
        String sql = "DELETE FROM assignments WHERE assignment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting assignment: " + e.getMessage(), e);
        }
    }

    // ---- Multithreading: Background Notification Thread ----

    /**
     * Starts the background due-date notification daemon thread.
     *
     * Multithreading: Uses ScheduledExecutorService (preferred over raw Thread)
     * to run checkDueDates() every 60 seconds.
     *
     * The daemon thread is automatically stopped when the JVM exits.
     *
     * @param callback a Consumer<String> that shows a notification in the GUI
     *                 (called via SwingUtilities.invokeLater in the GUI layer)
     */
    public void startNotificationThread(Consumer<String> callback) {
        this.notificationCallback = callback;

        // Create a single-threaded daemon scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "LMS-DueDateNotifier");
            t.setDaemon(true); // Daemon: won't block JVM shutdown
            return t;
        });

        // Run immediately, then every 60 seconds
        scheduler.scheduleAtFixedRate(this::checkDueDates, 0, 60, TimeUnit.SECONDS);
        System.out.println("[AssignmentService] Due-date notification thread started.");
    }

    /**
     * Stops the background scheduler gracefully.
     */
    public void stopNotificationThread() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("[AssignmentService] Notification thread stopped.");
        }
    }

    /**
     * Scans watched assignments and fires a notification for any due within 1 day.
     *
     * Synchronization: synchronized block ensures the list isn't modified
     * while we iterate it (the main thread may add new assignments concurrently).
     */
    private void checkDueDates() {
        // synchronized block — prevents ConcurrentModificationException
        synchronized (watchedAssignments) {
            for (Assignment a : watchedAssignments) {
                if (a.isDueWithinDays(1)) {
                    String message = "⚠ Assignment due soon: \"" + a.getTitle() +
                            "\" is due on " + a.getDueDate() + "!";
                    if (notificationCallback != null) {
                        notificationCallback.accept(message);
                    }
                }
            }
        }
    }
}
