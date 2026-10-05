package com.lms.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Course — represents a course in the LMS.
 *
 * Collections:
 * - lessons stored in a TreeMap<Integer, Lesson> (ordered by orderIndex)
 * - assignments stored in an ArrayList
 *
 * Encapsulation: all fields private, accessed via getters/setters.
 */
public class Course {

    private int courseId;
    private String title;
    private String description;
    private int teacherId;
    private String teacherName; // Denormalised for display
    private int maxStudents;

    // Collections: TreeMap keeps lessons sorted by their order index automatically
    private TreeMap<Integer, Lesson> lessons;

    // Collections: ArrayList for assignments
    private List<Assignment> assignments;

    // ---- Constructors ----

    public Course() {
        this.lessons = new TreeMap<>();
        this.assignments = new ArrayList<>();
    }

    public Course(int courseId, String title, String description,
            int teacherId, int maxStudents) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.maxStudents = maxStudents;
        this.lessons = new TreeMap<>();
        this.assignments = new ArrayList<>();
    }

    // ---- Getters and Setters ----

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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }

    // TreeMap getter — returns lessons in order-index order
    public TreeMap<Integer, Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(TreeMap<Integer, Lesson> lessons) {
        this.lessons = lessons;
    }

    public void addLesson(Lesson lesson) {
        // TreeMap ensures automatic ordering by orderIndex
        this.lessons.put(lesson.getOrderIndex(), lesson);
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public void addAssignment(Assignment a) {
        this.assignments.add(a);
    }

    @Override
    public String toString() {
        return title; // Used in JList/JComboBox display
    }
}
