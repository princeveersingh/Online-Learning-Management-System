package com.lms.service;

import com.lms.dao.LessonDAO;
import com.lms.model.Lesson;
import com.lms.util.InputValidator;

import java.util.List;

/**
 * LessonService — business logic for lesson management.
 */
public class LessonService {

    private final LessonDAO lessonDAO;

    public LessonService() {
        this.lessonDAO = new LessonDAO();
    }

    /**
     * Adds a new lesson to a course.
     */
    public Lesson addLesson(int courseId, String title, String content, int orderIndex) {
        if (!InputValidator.isNotBlank(title))
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        if (!InputValidator.isPositive(courseId))
            throw new IllegalArgumentException("Invalid course ID.");

        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        lesson.setTitle(InputValidator.sanitize(title));
        lesson.setContent(InputValidator.sanitize(content));
        lesson.setOrderIndex(orderIndex);

        lessonDAO.save(lesson);
        return lesson;
    }

    /**
     * Updates an existing lesson.
     */
    public void updateLesson(Lesson lesson) {
        if (!InputValidator.isNotBlank(lesson.getTitle())) {
            throw new IllegalArgumentException("Lesson title cannot be empty.");
        }
        lessonDAO.update(lesson);
    }

    /**
     * Deletes a lesson by ID.
     */
    public void deleteLesson(int lessonId) {
        lessonDAO.delete(lessonId);
    }

    /**
     * Returns all lessons for a course, ordered by orderIndex.
     */
    public List<Lesson> getLessonsByCourse(int courseId) {
        return lessonDAO.findByCourseId(courseId);
    }

    public Lesson getLessonById(int lessonId) {
        return lessonDAO.findById(lessonId);
    }
}
