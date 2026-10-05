package com.lms.database;

import com.lms.util.PasswordUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseInitializer — runs the DDL SQL script and seeds default data.
 *
 * On first application launch this class:
 * 1. Reads resources/database.sql and executes each statement (batch DDL).
 * 2. Checks whether default Teacher and Student accounts exist.
 * 3. Creates them if they don't (with properly hashed passwords).
 *
 * This demonstrates JDBC Statement and ResultSet usage.
 */
public class DatabaseInitializer {

    /**
     * Entry point — called by Main.java before the GUI launches.
     * Idempotent: safe to call on every startup (IF NOT EXISTS in SQL).
     */
    public static void initialize() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            runSqlScript(conn);
            seedDefaultAccounts(conn);
            System.out.println("[DatabaseInitializer] Database ready.");
        } catch (Exception e) {
            System.err.println("[DatabaseInitializer] Initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads resources/database.sql and executes each CREATE TABLE statement.
     * Splits on semicolons to handle multi-statement SQL files.
     */
    private static void runSqlScript(Connection conn) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder();

        // Try classpath resource first, then file system fallback
        InputStream in = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("database.sql");
        if (in == null) {
            java.io.File f = new java.io.File("resources/database.sql");
            in = new java.io.FileInputStream(f);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments
                if (line.trim().startsWith("--") || line.trim().isEmpty())
                    continue;
                sb.append(line).append("\n");
            }
        }

        // Execute each statement separated by semicolons
        String[] statements = sb.toString().split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
        System.out.println("[DatabaseInitializer] SQL schema applied.");
    }

    /**
     * Creates default Teacher and Student accounts if they don't exist.
     * Passwords are hashed using PasswordUtil.hashPassword() — never stored in
     * plain text.
     */
    private static void seedDefaultAccounts(Connection conn) throws SQLException {
        // Check if default teacher exists
        boolean teacherExists = accountExists(conn, "teacher@lms.com");
        if (!teacherExists) {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword("teacher123", salt);

            try (Statement stmt = conn.createStatement()) {
                // Insert into users table first
                stmt.execute(
                        "INSERT INTO users (name, email, password_hash, salt, role) VALUES " +
                                "('Demo Teacher', 'teacher@lms.com', '" + hash + "', '" + salt + "', 'TEACHER')");
                // Get the generated user_id
                ResultSet rs = stmt.executeQuery(
                        "SELECT user_id FROM users WHERE email = 'teacher@lms.com'");
                if (rs.next()) {
                    int tid = rs.getInt("user_id");
                    stmt.execute(
                            "INSERT INTO teachers (teacher_id, department, designation) VALUES " +
                                    "(" + tid + ", 'Computer Science', 'Assistant Professor')");
                }
            }
            System.out.println("[DatabaseInitializer] Default teacher account created: teacher@lms.com / teacher123");
        }

        // Check if default student exists
        boolean studentExists = accountExists(conn, "student@lms.com");
        if (!studentExists) {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword("student123", salt);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute(
                        "INSERT INTO users (name, email, password_hash, salt, role) VALUES " +
                                "('Demo Student', 'student@lms.com', '" + hash + "', '" + salt + "', 'STUDENT')");
                ResultSet rs = stmt.executeQuery(
                        "SELECT user_id FROM users WHERE email = 'student@lms.com'");
                if (rs.next()) {
                    int sid = rs.getInt("user_id");
                    stmt.execute(
                            "INSERT INTO students (student_id, roll_number, department, semester) VALUES " +
                                    "(" + sid + ", 'CS2024001', 'Computer Science', 3)");
                }
            }
            System.out.println("[DatabaseInitializer] Default student account created: student@lms.com / student123");
        }
    }

    /**
     * Checks whether a user with the given email already exists.
     */
    private static boolean accountExists(Connection conn, String email) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) AS cnt FROM users WHERE email = '" + email + "'");
            if (rs.next()) {
                return rs.getInt("cnt") > 0;
            }
        }
        return false;
    }
}
