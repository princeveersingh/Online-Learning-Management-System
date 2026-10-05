package com.lms.model;

import java.time.LocalDate;
import java.util.HashMap;

/**
 * Assignment — represents a course assignment with student submissions.
 *
 * Collections:
 * HashMap<Integer, Integer> — maps studentId → marksObtained.
 * HashMap gives O(1) lookup when grading a specific student.
 *
 * Encapsulation: private fields with controlled access.
 */
public class Assignment {

    private int assignmentId;
    private int courseId;
    private String title;
    private String description;
    private LocalDate dueDate;
    private int maxMarks;

    // Collections: HashMap<studentId, marksObtained> — fast O(1) grade lookup
    private HashMap<Integer, Integer> submissions;

    // ---- Constructors ----

    public Assignment() {
        this.submissions = new HashMap<>();
    }

    public Assignment(int assignmentId, int courseId, String title,
            String description, LocalDate dueDate, int maxMarks) {
        this.assignmentId = assignmentId;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxMarks = maxMarks;
        this.submissions = new HashMap<>();
    }

    // ---- Business Methods ----

    /**
     * Record or update a student's marks.
     */
    public void submitGrade(int studentId, int marks) {
        submissions.put(studentId, marks);
    }

    /**
     * Get a specific student's grade. Returns -1 if not graded.
     */
    public int getGrade(int studentId) {
        return submissions.getOrDefault(studentId, -1);
    }

    /**
     * Returns true if the assignment is due within the given number of days.
     * Used by the notification thread in AssignmentService.
     */
    public boolean isDueWithinDays(long days) {
        LocalDate today = LocalDate.now();
        LocalDate deadline = today.plusDays(days);
        return dueDate != null && !dueDate.isBefore(today) && !dueDate.isAfter(deadline);
    }

    // ---- Getters and Setters ----

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public HashMap<Integer, Integer> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(HashMap<Integer, Integer> submissions) {
        this.submissions = submissions;
    }

    @Override
    public String toString() {
        return title + " (Due: " + dueDate + ", Max: " + maxMarks + ")";
    }
}
