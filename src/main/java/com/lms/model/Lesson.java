package com.lms.model;

/**
 * Lesson — a single lesson/module belonging to a Course.
 *
 * Encapsulation: All fields private with getters/setters.
 * orderIndex is used as the key in Course's TreeMap<Integer, Lesson>.
 */
public class Lesson {

    private int lessonId;
    private int courseId;
    private String title;
    private String content;
    private int orderIndex; // Key in Course.lessons TreeMap

    // ---- Constructors ----

    public Lesson() {
    }

    public Lesson(int lessonId, int courseId, String title,
            String content, int orderIndex) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.title = title;
        this.content = content;
        this.orderIndex = orderIndex;
    }

    // ---- Getters and Setters ----

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    @Override
    public String toString() {
        return orderIndex + ". " + title; // Used in JList display
    }
}
