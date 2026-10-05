# Online Learning Management System (LMS)

A fully-featured **Java desktop application** for managing online courses, built with **Core Java**, **Java Swing GUI**, and **JDBC + MySQL**. Designed for academic evaluation demonstrating OOP principles, layered architecture, multithreading, and database integration.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔐 **Login System** | Role-based login for Student and Teacher |
| 📚 **Course Management** | Teachers can create, edit, and delete courses |
| 📖 **Lesson Management** | Add and organize lessons within courses |
| 📝 **Assignment Management** | Create assignments and grade student submissions |
| 🎓 **Enrollment Management** | Students enroll in courses; teachers view enrolled students |
| 🔔 **Due-Date Notifications** | Background thread alerts for upcoming assignment deadlines |
| 👤 **Student Dashboard** | View enrolled courses, grades, and assignments |
| 🧑‍🏫 **Teacher Dashboard** | Manage courses, lessons, assignments, and students |

---

## 🏗️ Architecture

```
com.lms/
├── model/       ← POJOs: User (abstract), Student, Teacher, Course, Lesson, Assignment, Enrollment
├── dao/         ← JDBC data access: UserDAO, CourseDAO, LessonDAO, EnrollmentDAO
├── service/     ← Business logic: UserService, CourseService, LessonService, AssignmentService, EnrollmentService
├── database/    ← DatabaseConnection (Singleton), DatabaseInitializer
├── exception/   ← Custom exceptions: UserNotFoundException, CourseNotFoundException
├── util/        ← Helpers: PasswordUtil (SHA-256), InputValidator
└── gui/         ← Swing frames: LoginFrame, StudentDashboard, TeacherDashboard, panels
```

---

## 🛠️ OOP Concepts Demonstrated

- **Encapsulation** — All model fields private with getters/setters
- **Inheritance** — `Student` and `Teacher` extend abstract `User`
- **Polymorphism** — `getDashboardTitle()` overridden in each subclass
- **Abstraction** — Abstract `User` class + `GenericDAO<T, ID>` interface
- **Generics** — `GenericDAO<T, ID>`, `ServiceResponse<T>` wrapper
- **Multithreading** — Background notification thread in `AssignmentService` (synchronized)
- **Collections** — `ArrayList`, `HashMap`, `TreeMap` used throughout

---

## 📋 Prerequisites

- **Java 17+** (JDK)
- **MySQL 8.0+**
- **MySQL Connector/J** (JDBC driver JAR)

---

## ⚙️ Setup Instructions

### 1. Database Setup

```sql
-- In MySQL:
CREATE DATABASE lms_db;
```

The tables are created automatically on first run by `DatabaseInitializer`.

### 2. Configuration

```bash
# Copy the example config
cp resources/config.properties.example resources/config.properties
```

Edit `resources/config.properties`:
```properties
db.host=localhost
db.port=3306
db.name=lms_db
db.user=root
db.password=YOUR_PASSWORD_HERE
```

> ⚠️ `config.properties` is in `.gitignore` — your credentials are never committed.

### 3. Add JDBC Driver

Download [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) and place the JAR in a `lib/` folder.

### 4. Compile

```bash
javac -cp "lib/mysql-connector-j-8.x.jar" -d out -sourcepath src/main/java \
  $(find src/main/java -name "*.java")
```

On Windows (PowerShell):
```powershell
$files = Get-ChildItem -Recurse -Filter "*.java" src\main\java | ForEach-Object { $_.FullName }
javac -cp "lib\mysql-connector-j-8.x.jar" -d out $files
```

### 5. Run

```bash
java -cp "out;lib/mysql-connector-j-8.x.jar" com.lms.Main
```

---

## 👤 Default Test Accounts

After first run, the database is seeded with:

| Role | Email | Password |
|---|---|---|
| Teacher | `teacher@lms.com` | `teacher123` |
| Student | `student@lms.com` | `student123` |

---

## 📁 Project Structure

```
Online-Learning-Management-System/
├── README.md
├── .gitignore
├── LICENSE
├── src/main/java/com/lms/
│   ├── Main.java
│   ├── model/
│   ├── service/
│   ├── dao/
│   ├── database/
│   ├── exception/
│   ├── util/
│   └── gui/
├── resources/
│   ├── database.sql
│   └── config.properties.example
├── docs/
│   └── system-architecture.md
└── tests/
```

---

## 🤝 Contributing

This is an academic project. Fork and extend as needed.

---

## 📄 License

MIT License — see [LICENSE](LICENSE).
