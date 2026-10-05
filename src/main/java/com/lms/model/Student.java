package com.lms.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Student — Concrete subclass of User (OOP: Inheritance + Polymorphism)
 *
 * Inheritance: Student inherits all User fields (userId, name, email, etc.)
 * and adds student-specific properties like rollNumber, department, semester.
 *
 * Polymorphism: Overrides getDashboardTitle() to return a student-specific
 * string.
 * When code calls user.getDashboardTitle() and the object is actually a
 * Student,
 * THIS version executes — runtime polymorphism (dynamic dispatch).
 *
 * Collections: Uses ArrayList to store the student's enrollments.
 */
public class Student extends User {

    // Student-specific fields (in addition to inherited User fields)
    private String rollNumber;
    private String department;
    private int semester;

    // Collections: ArrayList to hold enrollments
    private List<com.lms.model.Enrollment> enrollments;

    // ---- Constructors ----

    public Student() {
        super();
        this.enrollments = new ArrayList<>();
    }

    public Student(int userId, String name, String email,
            String passwordHash, String salt,
            String rollNumber, String department, int semester) {
        super(userId, name, email, passwordHash, salt, "STUDENT");
        this.rollNumber = rollNumber;
        this.department = department;
        this.semester = semester;
        this.enrollments = new ArrayList<>();
    }

    // ---- Polymorphism: override abstract methods ----

    /**
     * Runtime Polymorphism — this method is selected at runtime based on the
     * actual object type, even if the reference is of type User.
     */
    @Override
    public String getDashboardTitle() {
        return "Student Dashboard — " + getName();
    }

    @Override
    public String getRoleDescription() {
        return "Student | " + department + " | Semester " + semester;
    }

    // ---- Student-specific getters/setters ----

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
    }

    @Override
    public String toString() {
        return "Student{id=" + getUserId() + ", name='" + getName() +
                "', roll=" + rollNumber + ", dept=" + department + "}";
    }
}
