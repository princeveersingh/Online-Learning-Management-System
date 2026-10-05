package com.lms.dao;

import com.lms.database.DatabaseConnection;
import com.lms.exception.CourseNotFoundException;
import com.lms.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CourseDAO — CRUD operations for Course entities.
 * Implements GenericDAO<Course, Integer> using JDBC PreparedStatements.
 */
public class CourseDAO implements GenericDAO<Course, Integer> {

    private Connection getConn() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get DB connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Course course) {
        String sql = "INSERT INTO courses (title, description, teacher_id, max_students) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getTeacherId());
            ps.setInt(4, course.getMaxStudents());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                course.setCourseId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error saving course: " + e.getMessage(), e);
        }
    }

    @Override
    public Course findById(Integer courseId) {
        String sql = "SELECT c.*, u.name AS teacher_name FROM courses c " +
                "JOIN users u ON c.teacher_id = u.user_id WHERE c.course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding course: " + e.getMessage(), e);
        }
        throw new CourseNotFoundException(courseId);
    }

    @Override
    public List<Course> findAll() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS teacher_name FROM courses c " +
                "JOIN users u ON c.teacher_id = u.user_id ORDER BY c.title";
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching courses: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Returns courses belonging to a specific teacher.
     */
    public List<Course> findByTeacherId(int teacherId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS teacher_name FROM courses c " +
                "JOIN users u ON c.teacher_id = u.user_id WHERE c.teacher_id = ? ORDER BY c.title";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching courses by teacher: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Returns courses a student is enrolled in.
     */
    public List<Course> findByStudentId(int studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.*, u.name AS teacher_name FROM courses c " +
                "JOIN users u ON c.teacher_id = u.user_id " +
                "JOIN enrollments e ON e.course_id = c.course_id " +
                "WHERE e.student_id = ? ORDER BY c.title";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching student courses: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Course course) {
        String sql = "UPDATE courses SET title = ?, description = ?, max_students = ? WHERE course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getMaxStudents());
            ps.setInt(4, course.getCourseId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating course: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Integer courseId) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting course: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the number of students enrolled in a course.
     */
    public int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting enrollments: " + e.getMessage(), e);
        }
        return 0;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setTeacherId(rs.getInt("teacher_id"));
        c.setMaxStudents(rs.getInt("max_students"));
        try {
            c.setTeacherName(rs.getString("teacher_name"));
        } catch (SQLException ignored) {
        }
        return c;
    }
}
