package com.lms.model;

import java.time.LocalDate;

/**
 * Enrollment — represents a student being enrolled in a course.
 *
 * Implements Comparable<Enrollment> so lists of enrollments can
 * be sorted naturally by enroll date using Collections.sort().
 *
 * Encapsulation: all fields private.
 */
public class Enrollment implements Comparable<Enrollment> {

    private int enrollmentId;
    private int studentId;
    private int courseId;
    private String courseName; // Denormalised for display
    private LocalDate enrollDate;
    private String grade; // e.g. "A", "B+", "Pass"

    // ---- Constructors ----

    public Enrollment() {
    }

    public Enrollment(int enrollmentId, int studentId, int courseId,
            LocalDate enrollDate, String grade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.enrollDate = enrollDate;
        this.grade = grade;
    }

    // ---- Comparable — natural ordering by enroll date (most recent first) ----

    /**
     * Comparable: allows Collections.sort(enrollments) to work.
     * Most recent enrollment appears first.
     */
    @Override
    public int compareTo(Enrollment other) {
        if (this.enrollDate == null)
            return 1;
        if (other.enrollDate == null)
            return -1;
        return other.enrollDate.compareTo(this.enrollDate); // Descending
    }

    // ---- Getters and Setters ----

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public LocalDate getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(LocalDate enrollDate) {
        this.enrollDate = enrollDate;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Enrollment{student=" + studentId + ", course=" + courseId +
                ", date=" + enrollDate + ", grade=" + grade + "}";
    }
}
