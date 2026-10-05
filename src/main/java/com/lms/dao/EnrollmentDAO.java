package com.lms.dao;

import com.lms.database.DatabaseConnection;
import com.lms.model.Enrollment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * EnrollmentDAO — CRUD operations for Enrollment entities.
 * Implements GenericDAO<Enrollment, Integer>.
 */
public class EnrollmentDAO implements GenericDAO<Enrollment, Integer> {

    private Connection getConn() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get DB connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Enrollment e) {
        String sql = "INSERT INTO enrollments (student_id, course_id, enroll_date, grade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getStudentId());
            ps.setInt(2, e.getCourseId());
            ps.setDate(3, Date.valueOf(e.getEnrollDate() != null ? e.getEnrollDate() : LocalDate.now()));
            ps.setString(4, e.getGrade());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                e.setEnrollmentId(keys.getInt(1));
        } catch (SQLException ex) {
            throw new RuntimeException("Error saving enrollment: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Enrollment findById(Integer id) {
        String sql = "SELECT e.*, c.title AS course_name FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id WHERE e.enrollment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException ex) {
            throw new RuntimeException("Error finding enrollment: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public List<Enrollment> findAll() {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.title AS course_name FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id ORDER BY e.enroll_date DESC";
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching enrollments: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Returns all enrollments for a specific student.
     */
    public List<Enrollment> findByStudentId(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.title AS course_name FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id WHERE e.student_id = ? ORDER BY e.enroll_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching student enrollments: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Returns all enrollments for a specific course (by teacher to see their
     * students).
     */
    public List<Enrollment> findByCourseId(int courseId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT e.*, c.title AS course_name FROM enrollments e " +
                "JOIN courses c ON e.course_id = c.course_id WHERE e.course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException ex) {
            throw new RuntimeException("Error fetching course enrollments: " + ex.getMessage(), ex);
        }
        return list;
    }

    /**
     * Checks whether a student is already enrolled in a course.
     */
    public boolean isEnrolled(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1) > 0;
        } catch (SQLException ex) {
            throw new RuntimeException("Error checking enrollment: " + ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public void update(Enrollment e) {
        String sql = "UPDATE enrollments SET grade = ? WHERE enrollment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, e.getGrade());
            ps.setInt(2, e.getEnrollmentId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error updating enrollment: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error deleting enrollment: " + ex.getMessage(), ex);
        }
    }

    /**
     * Updates grade for a student in a specific course.
     */
    public void updateGrade(int studentId, int courseId, String grade) {
        String sql = "UPDATE enrollments SET grade = ? WHERE student_id = ? AND course_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, grade);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Error updating grade: " + ex.getMessage(), ex);
        }
    }

    private Enrollment mapRow(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setCourseId(rs.getInt("course_id"));
        Date d = rs.getDate("enroll_date");
        e.setEnrollDate(d != null ? d.toLocalDate() : LocalDate.now());
        e.setGrade(rs.getString("grade"));
        try {
            e.setCourseName(rs.getString("course_name"));
        } catch (SQLException ignored) {
        }
        return e;
    }
}
