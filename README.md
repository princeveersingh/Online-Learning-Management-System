# Online Learning Management System (LMS)

A Java-based desktop application for managing online learning activities such as courses, lessons, assignments, enrollments, and user management.

The project is developed using **Core Java, Java Swing, JDBC, and MySQL** and follows a layered architecture separating the model, service, DAO, database, utility, exception, and GUI components.

---

## Project Objectives

The main objectives of this project are:

- Provide a centralized platform for online learning management.
- Allow students to access courses and learning content.
- Allow teachers to manage courses, lessons, and assignments.
- Store application data securely in a MySQL database.
- Demonstrate Object-Oriented Programming concepts in Java.
- Implement database operations using JDBC.
- Provide a user-friendly Java Swing graphical interface.

---

## Key Features

### Student

- Student login
- Student dashboard
- View available courses
- Course enrollment
- Access lessons
- View assignments

### Teacher

- Teacher login
- Teacher dashboard
- Course management
- Lesson management
- Assignment management
- Student enrollment management

### Database

- MySQL database
- JDBC connectivity
- Database initialization
- DAO-based database operations
- External configuration for database credentials

---

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application development |
| Java Swing | Graphical User Interface |
| JDBC | Database connectivity |
| MySQL | Database management |
| Git | Version control |
| GitHub | Source code repository |

---

## Project Architecture

The project follows a layered architecture:

```text
                    Java Swing GUI
                          |
                          v
                    Service Layer
                          |
                          v
                      DAO Layer
                          |
                          v
                    JDBC / Database
                          |
                          v
                       MySQL

Utilizes Java Collections and Generics to manage application data efficiently.



Provides error handling, input validation, and database transaction management for reliable operations.

