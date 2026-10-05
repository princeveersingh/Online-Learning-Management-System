package com.lms.dao;

import com.lms.database.DatabaseConnection;
import com.lms.model.Lesson;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LessonDAO — CRUD operations for Lesson entities.
 * Implements GenericDAO<Lesson, Integer>.
 */
public class LessonDAO implements GenericDAO<Lesson, Integer> {

    private Connection getConn() {
        try {
            return DatabaseConnection.getInstance().getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get DB connection: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(Lesson lesson) {
        String sql = "INSERT INTO lessons (course_id, title, content, order_index) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lesson.getCourseId());
            ps.setString(2, lesson.getTitle());
            ps.setString(3, lesson.getContent());
            ps.setInt(4, lesson.getOrderIndex());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                lesson.setLessonId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error saving lesson: " + e.getMessage(), e);
        }
    }

    @Override
    public Lesson findById(Integer lessonId) {
        String sql = "SELECT * FROM lessons WHERE lesson_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error finding lesson: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Lesson> findAll() {
        List<Lesson> list = new ArrayList<>();
        try (Statement stmt = getConn().createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM lessons ORDER BY course_id, order_index")) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching lessons: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Returns lessons for a specific course, ordered by order_index (ascending).
     */
    public List<Lesson> findByCourseId(int courseId) {
        List<Lesson> list = new ArrayList<>();
        String sql = "SELECT * FROM lessons WHERE course_id = ? ORDER BY order_index";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching lessons for course: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void update(Lesson lesson) {
        String sql = "UPDATE lessons SET title = ?, content = ?, order_index = ? WHERE lesson_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getContent());
            ps.setInt(3, lesson.getOrderIndex());
            ps.setInt(4, lesson.getLessonId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating lesson: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Integer lessonId) {
        String sql = "DELETE FROM lessons WHERE lesson_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting lesson: " + e.getMessage(), e);
        }
    }

    private Lesson mapRow(ResultSet rs) throws SQLException {
        Lesson l = new Lesson();
        l.setLessonId(rs.getInt("lesson_id"));
        l.setCourseId(rs.getInt("course_id"));
        l.setTitle(rs.getString("title"));
        l.setContent(rs.getString("content"));
        l.setOrderIndex(rs.getInt("order_index"));
        return l;
    }
}
