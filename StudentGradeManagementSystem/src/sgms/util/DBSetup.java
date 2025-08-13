package sgms.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * One-off schema bootstrapper. Run this class ONCE to create the empty tables
 * inside School.accdb. Afterwards do NOT run again or you will get “table
 * already exists” errors.
 */
public class DBSetup {

    public static void main(String[] args) {
        try (Connection c = DBManager.get(); Statement s = c.createStatement()) {

            // 1 ───────── USERS ────────────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblUsers (
                    userId        AUTOINCREMENT PRIMARY KEY,
                    fullName      TEXT(50),
                    username      TEXT(30)  UNIQUE NOT NULL,
                    passwordHash  TEXT(80)  NOT NULL,
                    role          TEXT(20)  DEFAULT 'Teacher'
                )
            """);

            s.executeUpdate("""
                INSERT INTO tblUsers (fullName, username, passwordHash, role)
                VALUES ('Administrator', 'admin',
                        '6cd31521af231',
                        'Administrator')
            """);

            // 2 ───────── STUDENTS ────────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblStudents (
                    studentId   AUTOINCREMENT PRIMARY KEY,
                    firstName   TEXT(30)   NOT NULL,
                    lastName    TEXT(30)   NOT NULL,
                    gradeLevel  INTEGER    NOT NULL
                )
            """);

            // 3 ───────── COURSES ─────────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblCourses (
                    courseId    AUTOINCREMENT PRIMARY KEY,
                    courseCode  TEXT(10) UNIQUE NOT NULL,
                    courseName  TEXT(50) NOT NULL,
                    gradeLevel  INTEGER    NOT NULL
                )
            """);

            // 3b ───────── STUDENT-COURSE ENROLLMENTS ──────────────────────
            s.executeUpdate("""
                CREATE TABLE tblStudentCourses (
                    studentId  INTEGER NOT NULL,
                    courseId   INTEGER NOT NULL,
                    PRIMARY KEY (studentId, courseId),
                    FOREIGN KEY (studentId) REFERENCES tblStudents(studentId),
                    FOREIGN KEY (courseId)  REFERENCES tblCourses(courseId)
                )
            """);

            // 4 ───────── ASSIGNMENTS ────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblAssignments (
                    assignmentId  AUTOINCREMENT PRIMARY KEY,
                    courseId      INTEGER NOT NULL,
                    title         TEXT(50) NOT NULL,
                    maxMarks      INTEGER NOT NULL,
                    term          INTEGER NOT NULL,
                    dueDate       DATE    NOT NULL,
                    FOREIGN KEY (courseId) REFERENCES tblCourses(courseId)
                )
            """);

            // 5 ───────── GRADES ──────────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblGrades (
                    studentId     INTEGER NOT NULL,
                    assignmentId  INTEGER NOT NULL,
                    markAwarded   INTEGER,
                    PRIMARY KEY (studentId, assignmentId),
                    FOREIGN KEY (studentId)    REFERENCES tblStudents(studentId),
                    FOREIGN KEY (assignmentId) REFERENCES tblAssignments(assignmentId)
                )
            """);

            // 6 ───────── FINAL GRADES ────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblFinalGrades (
                    studentId  INTEGER PRIMARY KEY,
                    term1      INTEGER,
                    term2      INTEGER,
                    term3      INTEGER,
                    term4      INTEGER,
                    finalGrade INTEGER,
                    FOREIGN KEY (studentId) REFERENCES tblStudents(studentId)
                )
            """);

            // 7 ───────── ATTENDANCE ──────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblAttendance (
                    studentId   INTEGER NOT NULL,
                    courseId    INTEGER NOT NULL,
                    lessonDate  DATE    NOT NULL,
                    present     YESNO   DEFAULT FALSE,
                    PRIMARY KEY (studentId, courseId, lessonDate),
                    FOREIGN KEY (studentId) REFERENCES tblStudents(studentId),
                    FOREIGN KEY (courseId)  REFERENCES tblCourses(courseId)
                )
            """);

            // 8 ───────── FEEDBACK ────────────────────────────────────────────
            s.executeUpdate("""
                CREATE TABLE tblFeedback (
                    feedbackId  AUTOINCREMENT PRIMARY KEY,
                    studentId   INTEGER NOT NULL,
                    courseId    INTEGER NOT NULL,
                    note        MEMO,
                    entryDate   DATE DEFAULT DATE(),
                    FOREIGN KEY (studentId) REFERENCES tblStudents(studentId),
                    FOREIGN KEY (courseId)  REFERENCES tblCourses(courseId)
                )
            """);

            System.out.println("✅  Schema installed successfully.");
        } catch (SQLException e) {
            System.err.println("Schema creation failed!");
            e.printStackTrace();
        }
    }

    private DBSetup() {
    }
}
