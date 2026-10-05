package com.lms.service;

import com.lms.dao.EnrollmentDAO;
import com.lms.model.Enrollment;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * EnrollmentService — business logic for student enrollment.
 *
 * Prevents duplicate enrollment and checks course capacity.
 * Returns sorted enrollment lists (Comparable<Enrollment>).
 */
public class EnrollmentService {

    private final EnrollmentDAO enrollmentDAO;
    private final CourseService courseService;

    public EnrollmentService() {
        this.enrollmentDAO = new EnrollmentDAO();
        this.courseService = new CourseService();
    }

    /**
     * Enrolls a student in a course.
     * Checks for duplicate enrollment and course capacity before proceeding.
     * courseService.hasCapacity() is synchronized — safe under concurrent access.
     */
    public Enrollment enrollStudent(int studentId, int courseId) {
        // Check: already enrolled?
        if (enrollmentDAO.isEnrolled(studentId, courseId)) {
            throw new IllegalStateException("You are already enrolled in this course.");
        }

        // Check: course capacity (synchronized in CourseService)
        if (!courseService.hasCapacity(courseId)) {
            throw new IllegalStateException("This course has reached its maximum enrollment limit.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setEnrollDate(LocalDate.now());
        enrollment.setGrade(null);

        enrollmentDAO.save(enrollment);
        return enrollment;
    }

    /**
     * Removes a student's enrollment from a course.
     */
    public void unenrollStudent(int studentId, int courseId) {
        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(studentId);
        for (Enrollment e : enrollments) {
            if (e.getCourseId() == courseId) {
                enrollmentDAO.delete(e.getEnrollmentId());
                return;
            }
        }
        throw new IllegalStateException("Enrollment not found for this student and course.");
    }

    /**
     * Returns all enrollments for a student, sorted by date (most recent first).
     * Demonstrates Comparable — Collections.sort() uses Enrollment.compareTo().
     */
    public List<Enrollment> getStudentEnrollments(int studentId) {
        List<Enrollment> enrollments = enrollmentDAO.findByStudentId(studentId);
        Collections.sort(enrollments); // Uses Enrollment.compareTo() — Comparable
        return enrollments;
    }

    public List<Enrollment> getCourseEnrollments(int courseId) {
        return enrollmentDAO.findByCourseId(courseId);
    }

    /**
     * Updates the final grade for a student in a course.
     */
    public void updateGrade(int studentId, int courseId, String grade) {
        enrollmentDAO.updateGrade(studentId, courseId, grade);
    }

    public boolean isEnrolled(int studentId, int courseId) {
        return enrollmentDAO.isEnrolled(studentId, courseId);
    }
}
