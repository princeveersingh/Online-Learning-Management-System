package com.lms.dao;

import com.lms.database.DatabaseConnection;
import com.lms.exception.UserNotFoundException;
import com.lms.model.Student;
import com.lms.model.Teacher;
import com.lms.model.User;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — Data Access Object for User entities (Student + Teacher).
 *
 * Implements GenericDAO<User, Integer> — the Generics interface.
 * All SQL uses PreparedStatement to prevent SQL injection.
 *
 * JDBC Pattern used:
 * Connection → PreparedStatement → ResultSet → POJO mapping
 */
public class UserDAO implements GenericDAO<User, Integer> {

    private Connection getConn() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get DB connection: " + e.getMessage(), e);
        }
    }

    // ---- Save (INSERT) ----

    /**
     * Saves a new User to the 'users' table and the corresponding secondary
     * table (students or teachers). Note: password_hash and salt must be set before
     * calling.
     */
    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (name, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setString(5, user.getRole());
            ps.executeUpdate();

            // Retrieve auto-generated user_id
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                user.setUserId(keys.getInt(1));
            }

            // Insert into sub-table based on role
            if (user instanceof Student) {
                saveStudent((Student) user);
            } else if (user instanceof Teacher) {
                saveTeacher((Teacher) user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    private void saveStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (student_id, roll_number, department, semester) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getRollNumber());
            ps.setString(3, s.getDepartment());
            ps.setInt(4, s.getSemester());
            ps.executeUpdate();
        }
    }

    private void saveTeacher(Teacher t) throws SQLException {
        String sql = "INSERT INTO teachers (teacher_id, department, designation) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, t.getUserId());
            ps.setString(2, t.getDepartment());
            ps.setString(3, t.getDesignation());
            ps.executeUpdate();
        }
    }

    // ---- FindById (SELECT by PK) ----

    @Override
    public User findById(Integer userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Finds a user by email address — used for login.
     */
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
        throw new UserNotFoundException(email);
    }

    // ---- FindAll (SELECT all) ----

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name";
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users: " + e.getMessage(), e);
        }
        return users;
    }

    /**
     * Returns all Students (role = 'STUDENT') optionally joined with students
     * table.
     */
    public List<Student> findAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT u.*, s.roll_number, s.department, s.semester " +
                "FROM users u JOIN students s ON u.user_id = s.student_id " +
                "ORDER BY u.name";
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                students.add(mapRowToStudent(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching students: " + e.getMessage(), e);
        }
        return students;
    }

    /**
     * Returns all Teachers (role = 'TEACHER') joined with teachers table.
     */
    public List<Teacher> findAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT u.*, t.department, t.designation " +
                "FROM users u JOIN teachers t ON u.user_id = t.teacher_id " +
                "ORDER BY u.name";
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                teachers.add(mapRowToTeacher(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching teachers: " + e.getMessage(), e);
        }
        return teachers;
    }

    // ---- Update (UPDATE) ----

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, password_hash = ?, salt = ? WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getSalt());
            ps.setInt(5, user.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    // ---- Delete (DELETE) ----

    @Override
    public void delete(Integer userId) {
        // CASCADE delete in DB handles students/teachers sub-tables
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    // ---- Private mapping helpers ----

    /**
     * Maps a ResultSet row to the appropriate User subtype based on 'role' column.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        if ("STUDENT".equals(role)) {
            return mapRowToStudentBasic(rs);
        } else {
            return mapRowToTeacherBasic(rs);
        }
    }

    private Student mapRowToStudentBasic(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setUserId(rs.getInt("user_id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPasswordHash(rs.getString("password_hash"));
        s.setSalt(rs.getString("salt"));
        s.setRole("STUDENT");
        return s;
    }

    private Teacher mapRowToTeacherBasic(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setUserId(rs.getInt("user_id"));
        t.setName(rs.getString("name"));
        t.setEmail(rs.getString("email"));
        t.setPasswordHash(rs.getString("password_hash"));
        t.setSalt(rs.getString("salt"));
        t.setRole("TEACHER");
        return t;
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student s = mapRowToStudentBasic(rs);
        try {
            s.setRollNumber(rs.getString("roll_number"));
            s.setDepartment(rs.getString("department"));
            s.setSemester(rs.getInt("semester"));
        } catch (SQLException ignored) {
        }
        return s;
    }

    private Teacher mapRowToTeacher(ResultSet rs) throws SQLException {
        Teacher t = mapRowToTeacherBasic(rs);
        try {
            t.setDepartment(rs.getString("department"));
            t.setDesignation(rs.getString("designation"));
        } catch (SQLException ignored) {
        }
        return t;
    }
}
