package sgms.dao.impl;

import sgms.dao.StudentDAO;
import sgms.model.Student;
import sgms.model.Course;
import sgms.dao.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic JDBC implementation of {@link StudentDAO} using the UCanAccess library.
 * The code tries to stay as simple as possible so it is easy to follow.
 */
public class UcanaccessStudentDAO implements StudentDAO {

    // add a new student to the table
    @Override
    public Student add(Student s) throws SQLException {
        String sql = "INSERT INTO tblStudents(firstName, lastName, gradeLevel) VALUES (?,?,?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.executeUpdate();

            // get the auto-generated id
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setStudentId(keys.getInt(1));
                }
            }
            return s;
        }
    }

    // update the student information
    @Override
    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE tblStudents SET firstName=?, lastName=?, gradeLevel=? WHERE studentId=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.setInt(4, s.getStudentId());
            return ps.executeUpdate() == 1;
        }
    }

    // remove a student and any enrollments
    @Override
    public boolean delete(int id) throws SQLException {
        String delEnrollments = "DELETE FROM tblStudentCourses WHERE studentId=?";
        String delStudent = "DELETE FROM tblStudents WHERE studentId=?";
        try (Connection c = Db.get();
             PreparedStatement psEnroll = c.prepareStatement(delEnrollments);
             PreparedStatement psStudent = c.prepareStatement(delStudent)) {

            ensureEnrollmentTableExists(c);
            psEnroll.setInt(1, id);
            psEnroll.executeUpdate();

            psStudent.setInt(1, id);
            return psStudent.executeUpdate() == 1;
        }
    }

    // find a single student; returns null if not found
    @Override
    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM tblStudents WHERE studentId=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        }
    }

    // list all students
    @Override
    public List<Student> findAll() throws SQLException {
        String sql = "SELECT * FROM tblStudents ORDER BY lastName";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    // students in a given grade
    @Override
    public List<Student> findByGradeLevel(int gradeLevel) throws SQLException {
        String sql = "SELECT * FROM tblStudents WHERE gradeLevel = ? ORDER BY lastName";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
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

    // distinct grade levels
    @Override
    public List<Integer> findGradeLevels() throws SQLException {
        String sql = "SELECT DISTINCT gradeLevel FROM tblStudents ORDER BY gradeLevel";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Integer> grades = new ArrayList<>();
            while (rs.next()) {
                grades.add(rs.getInt("gradeLevel"));
            }
            return grades;
        }
    }

    // all available courses
    @Override
    public List<Course> findCourses() throws SQLException {
        String sql = "SELECT courseId, courseCode, courseName, gradeLevel FROM tblCourses ORDER BY courseName";
        try (Connection c = Db.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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

    // enroll a student into a course
    @Override
    public boolean enrollStudentInCourse(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO tblStudentCourses(studentId, courseId) VALUES (?, ?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    // students enrolled in a course
    @Override
    public List<Student> findByCourse(int courseId) throws SQLException {
        String sql = "SELECT s.* FROM tblStudents s JOIN tblStudentCourses sc ON s.studentId = sc.studentId WHERE sc.courseId = ? ORDER BY s.lastName";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
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

    // remove a student from a course
    @Override
    public boolean removeStudentFromCourse(int studentId, int courseId) throws SQLException {
        String sql = "DELETE FROM tblStudentCourses WHERE studentId = ? AND courseId = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    // helper method to build a Student from a ResultSet
    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("studentId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getInt("gradeLevel"));
    }

    // make sure the join table exists
    private void ensureEnrollmentTableExists(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE tblStudentCourses (" +
                    "studentId INTEGER NOT NULL," +
                    "courseId INTEGER NOT NULL," +
                    "PRIMARY KEY (studentId, courseId)," +
                    "FOREIGN KEY (studentId) REFERENCES tblStudents(studentId)," +
                    "FOREIGN KEY (courseId) REFERENCES tblCourses(courseId))");
        } catch (SQLException ex) {
            if (!ex.getMessage().contains("already exists")) {
                throw ex;
            }
        }
    }
}