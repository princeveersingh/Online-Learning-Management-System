package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.exception.CourseNotFoundException;
import com.lms.model.Course;
import com.lms.util.InputValidator;

import java.util.List;

/**
 * CourseService — business logic for course management.
 *
 * Multithreading: enrollStudent() is synchronized to safely check capacity
 * across concurrent threads — prevents over-enrollment race conditions.
 */
public class CourseService {

    private final CourseDAO courseDAO;

    public CourseService() {
        this.courseDAO = new CourseDAO();
    }

    /**
     * Creates a new course for the given teacher.
     */
    public Course createCourse(String title, String description, int teacherId, int maxStudents) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Course title cannot be empty.");
        if (!InputValidator.isPositive(teacherId))
            throw new IllegalArgumentException("Invalid teacher ID.");

        Course course = new Course();
        course.setTitle(InputValidator.sanitize(title));
        course.setDescription(InputValidator.sanitize(description));
        course.setTeacherId(teacherId);
        course.setMaxStudents(maxStudents > 0 ? maxStudents : 50);

        courseDAO.save(course);
        return course;
    }

    /**
     * Updates an existing course.
     * Throws CourseNotFoundException if the course doesn't exist.
     */
    public void updateCourse(Course course) {
        if (!InputValidator.isNotBlank(course.getTitle())) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        // Verify course exists first
        getCourseById(course.getCourseId());
        courseDAO.update(course);
    }

    /**
     * Deletes a course by ID.
     */
    public void deleteCourse(int courseId) {
        getCourseById(courseId); // Throws if not found
        courseDAO.delete(courseId);
    }

    /**
     * Retrieves a course by ID. Throws CourseNotFoundException if missing.
     */
    public Course getCourseById(int courseId) {
        Course course = courseDAO.findById(courseId);
        if (course == null)
            throw new CourseNotFoundException(courseId);
        return course;
    }

    public List<Course> getAllCourses() {
        return courseDAO.findAll();
    }

    public List<Course> getCoursesByTeacher(int teacherId) {
        return courseDAO.findByTeacherId(teacherId);
    }

    public List<Course> getCoursesByStudent(int studentId) {
        return courseDAO.findByStudentId(studentId);
    }

    /**
     * Synchronized capacity check — prevents race conditions when multiple
     * students try to enroll in the last spot simultaneously.
     * (Multithreading: synchronization on the service method)
     */
    public synchronized boolean hasCapacity(int courseId) {
        Course course = getCourseById(courseId);
        int enrolled = courseDAO.getEnrollmentCount(courseId);
        return enrolled < course.getMaxStudents();
    }

    public int getEnrollmentCount(int courseId) {
        return courseDAO.getEnrollmentCount(courseId);
    }
}
