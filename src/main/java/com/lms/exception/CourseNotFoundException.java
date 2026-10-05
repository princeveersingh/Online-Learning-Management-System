package com.lms.exception;

/**
 * CourseNotFoundException — Custom Exception (OOP: Exception Handling)
 *
 * Thrown by CourseService/CourseDAO when a requested course does not exist.
 * Provides meaningful error messages for the GUI to display.
 */
public class CourseNotFoundException extends RuntimeException {

    private final int courseId;

    public CourseNotFoundException(int courseId) {
        super("Course not found with ID: " + courseId);
        this.courseId = courseId;
    }

    public CourseNotFoundException(String title) {
        super("Course not found: " + title);
        this.courseId = -1;
    }

    public int getCourseId() {
        return courseId;
    }
}
