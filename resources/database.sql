-- ============================================================
-- Online Learning Management System — Database Schema
-- MySQL 8.0+
-- Run once; DatabaseInitializer.java calls this automatically.
-- ============================================================

CREATE DATABASE IF NOT EXISTS lms_db;
USE lms_db;

-- -------------------------
-- Users table (base entity)
-- -------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt          VARCHAR(64)  NOT NULL,
    role          ENUM('STUDENT','TEACHER') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- -------------------------
-- Students (extends users)
-- -------------------------
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY,           -- same as user_id
    roll_number VARCHAR(20) UNIQUE,
    department  VARCHAR(100),
    semester    INT DEFAULT 1,
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------
-- Teachers (extends users)
-- -------------------------
CREATE TABLE IF NOT EXISTS teachers (
    teacher_id INT PRIMARY KEY,           -- same as user_id
    department  VARCHAR(100),
    designation VARCHAR(100),
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------
-- Courses
-- -------------------------
CREATE TABLE IF NOT EXISTS courses (
    course_id   INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id  INT NOT NULL,
    max_students INT DEFAULT 50,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- -------------------------
-- Lessons
-- -------------------------
CREATE TABLE IF NOT EXISTS lessons (
    lesson_id    INT AUTO_INCREMENT PRIMARY KEY,
    course_id    INT NOT NULL,
    title        VARCHAR(200) NOT NULL,
    content      TEXT,
    order_index  INT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- -------------------------
-- Assignments
-- -------------------------
CREATE TABLE IF NOT EXISTS assignments (
    assignment_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id     INT NOT NULL,
    title         VARCHAR(200) NOT NULL,
    description   TEXT,
    due_date      DATE NOT NULL,
    max_marks     INT DEFAULT 100,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
);

-- -------------------------
-- Assignment Submissions
-- -------------------------
CREATE TABLE IF NOT EXISTS submissions (
    submission_id  INT AUTO_INCREMENT PRIMARY KEY,
    assignment_id  INT NOT NULL,
    student_id     INT NOT NULL,
    marks_obtained INT,
    submitted_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_submission (assignment_id, student_id)
);

-- -------------------------
-- Enrollments
-- -------------------------
CREATE TABLE IF NOT EXISTS enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id    INT NOT NULL,
    course_id     INT NOT NULL,
    enroll_date   DATE DEFAULT (CURDATE()),
    grade         VARCHAR(5),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id)  REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY uq_enrollment (student_id, course_id)
);

-- ============================================================
-- Seed Data — default accounts
-- Passwords are SHA-256(salt + password):
--   teacher@lms.com  / teacher123
--   student@lms.com  / student123
-- (Actual hashes generated at runtime by PasswordUtil)
-- ============================================================
-- Seeding is handled by DatabaseInitializer.java at first launch.
