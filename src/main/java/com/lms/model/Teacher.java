package com.lms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Teacher — Concrete subclass of User (OOP: Inheritance + Polymorphism)
 *
 * Inherits all User properties and adds department, designation.
 * Overrides getDashboardTitle() demonstrating runtime polymorphism.
 */
public class Teacher extends User {

    private String department;
    private String designation;

    // Collections: ArrayList of courses this teacher manages
    private List<Course> teachingCourses;

    // ---- Constructors ----

    public Teacher() {
        super();
        this.teachingCourses = new ArrayList<>();
    }

    public Teacher(int userId, String name, String email,
            String passwordHash, String salt,
            String department, String designation) {
        super(userId, name, email, passwordHash, salt, "TEACHER");
        this.department = department;
        this.designation = designation;
        this.teachingCourses = new ArrayList<>();
    }

    // ---- Polymorphism: override abstract methods ----

    @Override
    public String getDashboardTitle() {
        return "Teacher Dashboard — " + getName();
    }

    @Override
    public String getRoleDescription() {
        return designation + " | " + department;
    }

    // ---- Teacher-specific getters/setters ----

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<Course> getTeachingCourses() {
        return teachingCourses;
    }

    public void setTeachingCourses(List<Course> courses) {
        this.teachingCourses = courses;
    }

    public void addCourse(Course course) {
        this.teachingCourses.add(course);
    }

    @Override
    public String toString() {
        return "Teacher{id=" + getUserId() + ", name='" + getName() +
                "', dept=" + department + ", designation=" + designation + "}";
    }
}
