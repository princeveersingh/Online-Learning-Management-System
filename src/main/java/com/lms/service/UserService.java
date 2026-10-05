package com.lms.service;

import com.lms.dao.UserDAO;
import com.lms.exception.UserNotFoundException;
import com.lms.model.Student;
import com.lms.model.Teacher;
import com.lms.model.User;
import com.lms.util.InputValidator;
import com.lms.util.PasswordUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserService — business logic for user registration and authentication.
 *
 * Generics: Uses HashMap<Integer, User> as an in-memory cache to avoid
 * repeated DB lookups for the same user within a session.
 *
 * Encapsulation: The cache is private; services are the only entry point.
 */
public class UserService {

    private final UserDAO userDAO;

    // Collections: HashMap as in-memory user cache (userId -> User)
    // Generics: HashMap<Integer, User> — typed collection
    private final Map<Integer, User> userCache = new HashMap<>();

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Authenticates a user by email and plain-text password.
     * Uses bcrypt-style (SHA-256 + salt) verification via PasswordUtil.
     *
     * @return the authenticated User object (Student or Teacher)
     * @throws UserNotFoundException    if no account matches the email
     * @throws IllegalArgumentException if password is wrong
     */
    public User login(String email, String plainPassword) {
        if (!InputValidator.isNotBlank(email) || !InputValidator.isNotBlank(plainPassword)) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        User user = userDAO.findByEmail(email); // Throws UserNotFoundException if not found

        boolean valid = PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash(), user.getSalt());
        if (!valid) {
            throw new IllegalArgumentException("Incorrect password. Please try again.");
        }

        // Cache the authenticated user
        userCache.put(user.getUserId(), user);
        return user;
    }

    /**
     * Registers a new Student account.
     */
    public Student registerStudent(String name, String email, String plainPassword,
            String rollNumber, String department, int semester) {
        validateRegistration(name, email, plainPassword);

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        Student s = new Student();
        s.setName(InputValidator.sanitize(name));
        s.setEmail(InputValidator.sanitize(email));
        s.setPasswordHash(hash);
        s.setSalt(salt);
        s.setRollNumber(rollNumber);
        s.setDepartment(department);
        s.setSemester(semester);

        userDAO.save(s);
        userCache.put(s.getUserId(), s);
        return s;
    }

    /**
     * Registers a new Teacher account.
     */
    public Teacher registerTeacher(String name, String email, String plainPassword,
            String department, String designation) {
        validateRegistration(name, email, plainPassword);

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(plainPassword, salt);

        Teacher t = new Teacher();
        t.setName(InputValidator.sanitize(name));
        t.setEmail(InputValidator.sanitize(email));
        t.setPasswordHash(hash);
        t.setSalt(salt);
        t.setDepartment(department);
        t.setDesignation(designation);

        userDAO.save(t);
        userCache.put(t.getUserId(), t);
        return t;
    }

    /**
     * Returns a User by ID — checks cache first, then DB.
     */
    public User getUserById(int userId) {
        // Check cache first (performance optimisation)
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }
        User user = userDAO.findById(userId);
        if (user == null)
            throw new UserNotFoundException(userId);
        userCache.put(userId, user);
        return user;
    }

    public List<Student> getAllStudents() {
        return userDAO.findAllStudents();
    }

    public List<Teacher> getAllTeachers() {
        return userDAO.findAllTeachers();
    }

    private void validateRegistration(String name, String email, String password) {
        if (!InputValidator.isNotBlank(name))
            throw new IllegalArgumentException("Name cannot be empty.");
        if (!InputValidator.isValidEmail(email))
            throw new IllegalArgumentException("Invalid email address.");
        if (!InputValidator.isValidPassword(password))
            throw new IllegalArgumentException("Password must be at least 6 characters.");
    }
}
