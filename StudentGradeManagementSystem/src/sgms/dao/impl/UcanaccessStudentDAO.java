package sgms.dao.impl;

import sgms.dao.StudentDAO;
import sgms.model.Student;
import sgms.util.DBManager;
import sgms.model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UCanAccess-backed implementation of StudentDAO. Uses prepared statements for
 * safety and speed.
 */
public class UcanaccessStudentDAO implements StudentDAO {

    /* ─────────────────────── CREATE ─────────────────────────────── */
    @Override
    public Student add(Student s) throws SQLException {
        final String sql = """
            INSERT INTO tblStudents(firstName, lastName, gradeLevel)
            VALUES (?,?,?)
        """;
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setStudentId(keys.getInt(1));
                }
            }
            return s;
        }
    }

    /* ─────────────────────── UPDATE ─────────────────────────────── */
    @Override
    public boolean update(Student s) throws SQLException {
        final String sql = """
            UPDATE tblStudents
            SET firstName = ?, lastName = ?, gradeLevel = ?
            WHERE studentId = ?
        """;
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.setInt(4, s.getStudentId());
            return ps.executeUpdate() == 1;
        }
    }

    /* ─────────────────────── DELETE ─────────────────────────────── */
    @Override
    public boolean delete(int id) throws SQLException {
        final String delEnrollments = "DELETE FROM tblStudentCourses WHERE studentId = ?";
        final String delStudent = "DELETE FROM tblStudents WHERE studentId = ?";
        try (Connection c = DBManager.get(); PreparedStatement psEnroll = c.prepareStatement(delEnrollments); PreparedStatement psStudent = c.prepareStatement(delStudent)) {

            ensureEnrollmentTableExists(c);
            psEnroll.setInt(1, id);
            psEnroll.executeUpdate();

            psStudent.setInt(1, id);
            return psStudent.executeUpdate() == 1;
        }
    }

    /* ─────────────────────── READ (single) ──────────────────────── */
    @Override
    public Optional<Student> findById(int id) throws SQLException {
        final String sql = "SELECT * FROM tblStudents WHERE studentId = ?";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    /* ─────────────────────── READ (all) ─────────────────────────── */
    @Override
    public List<Student> findAll() throws SQLException {
        final String sql = "SELECT * FROM tblStudents ORDER BY lastName";
        try (Connection c = DBManager.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    @Override
    public List<Student> findByGradeLevel(int gradeLevel) throws SQLException {
        final String sql = "SELECT * FROM tblStudents WHERE gradeLevel = ? ORDER BY lastName";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, gradeLevel);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    @Override
    public List<Integer> findGradeLevels() throws SQLException {
        final String sql = "SELECT DISTINCT gradeLevel FROM tblStudents ORDER BY gradeLevel";
        try (Connection c = DBManager.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            List<Integer> grades = new ArrayList<>();
            while (rs.next()) {
                grades.add(rs.getInt("gradeLevel"));
            }
            return grades;
        }
    }

    @Override
    public List<Course> findCourses() throws SQLException {
        final String sql = "SELECT courseId, courseCode, courseName, gradeLevel FROM tblCourses ORDER BY courseName";
        try (Connection c = DBManager.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            List<Course> courses = new ArrayList<>();
            while (rs.next()) {
                courses.add(new Course(rs.getInt("courseId"),
                        rs.getString("courseCode"),
                        rs.getString("courseName"),
                        rs.getInt("gradeLevel")));
            }
            return courses;
        }
    }

    @Override
    public boolean enrollStudentInCourse(int studentId, int courseId) throws SQLException {
        final String sql = "INSERT INTO tblStudentCourses(studentId, courseId) VALUES (?, ?)";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public List<Student> findByCourse(int courseId) throws SQLException {
        final String sql = """
            SELECT s.* FROM tblStudents s
            JOIN tblStudentCourses sc ON s.studentId = sc.studentId
            WHERE sc.courseId = ?
            ORDER BY s.lastName
        """;
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    @Override
    public boolean removeStudentFromCourse(int studentId, int courseId) throws SQLException {
        final String sql = "DELETE FROM tblStudentCourses WHERE studentId = ? AND courseId = ?";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    /* ─────────────────────── Helper ─────────────────────────────── */
    /**
     * Converts the current row of a ResultSet into a Student object.
     */
    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("studentId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getInt("gradeLevel")
        );
    }

    /**
     * Ensures the student-course join table exists (creates it if needed).
     */
    private void ensureEnrollmentTableExists(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE tblStudentCourses (
                    studentId INTEGER NOT NULL,
                    courseId  INTEGER NOT NULL,
                    PRIMARY KEY (studentId, courseId),
                    FOREIGN KEY (studentId) REFERENCES tblStudents(studentId),
                    FOREIGN KEY (courseId)  REFERENCES tblCourses(courseId)
                )
            """);
        } catch (SQLException ex) {
            // Table already exists
            if (!ex.getMessage().contains("already exists")) {
                throw ex;
            }
        }
    }
}
