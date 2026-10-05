package com.lms.model;

/**
 * User — Abstract Base Class (OOP: Abstraction + Encapsulation)
 *
 * Abstraction: Declares the abstract method getDashboardTitle() so each
 * subclass is forced to define its own title — we don't know the implementation
 * here, only the contract.
 *
 * Encapsulation: All fields are private. Access is only through
 * controlled getters/setters, protecting data integrity.
 *
 * Inheritance: Student and Teacher both extend this class, inheriting
 * these fields and behaviours while adding their own specialisations.
 */
public abstract class User {

    // Encapsulation — private fields
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String salt;
    private String role; // "STUDENT" or "TEACHER"

    // ---- Constructors ----

    public User() {
    }

    public User(int userId, String name, String email,
            String passwordHash, String salt, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    // ---- Abstract method — Polymorphism + Abstraction ----

    /**
     * Polymorphism: Each subclass returns a different dashboard title.
     * The GUI can call user.getDashboardTitle() without knowing the concrete type.
     */
    public abstract String getDashboardTitle();

    /**
     * Abstract method to get a brief description of the user's role.
     * Demonstrates abstraction — subclasses define the meaning.
     */
    public abstract String getRoleDescription();

    // ---- Getters and Setters (Encapsulation) ----

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // ---- Overriding Object.toString() ----

    @Override
    public String toString() {
        return "User{id=" + userId + ", name='" + name + "', role=" + role + "}";
    }
}
