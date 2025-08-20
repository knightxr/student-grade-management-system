package sgms.dao.impl;

import sgms.dao.DB;
import sgms.dao.StudentDAO;
import sgms.model.Course;
import sgms.model.Student;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Student DAO using UCanAccess (MS Access).
 * Uses parameterised SQL and try-with-resources.
 */
public class UcanaccessStudentDAO implements StudentDAO {

    private static final String INSERT_STUDENT =
            "INSERT INTO tblStudents(firstName, lastName, gradeLevel) VALUES (?,?,?)";

    private static final String UPDATE_STUDENT =
            "UPDATE tblStudents SET firstName=?, lastName=?, gradeLevel=? WHERE studentId=?";

    private static final String DELETE_ENROLLMENTS =
            "DELETE FROM tblStudentCourses WHERE studentId=?";

    private static final String DELETE_STUDENT =
            "DELETE FROM tblStudents WHERE studentId=?";

    private static final String SELECT_STUDENT_BY_ID =
            "SELECT studentId, firstName, lastName, gradeLevel FROM tblStudents WHERE studentId=?";

    private static final String SELECT_ALL_STUDENTS =
            "SELECT studentId, firstName, lastName, gradeLevel FROM tblStudents ORDER BY lastName";

    private static final String SELECT_STUDENTS_BY_GRADE =
            "SELECT studentId, firstName, lastName, gradeLevel FROM tblStudents WHERE gradeLevel = ? ORDER BY lastName";

    private static final String SELECT_GRADE_LEVELS =
            "SELECT DISTINCT gradeLevel FROM tblStudents ORDER BY gradeLevel";

    private static final String SELECT_COURSES =
            "SELECT courseId, courseCode, courseName, gradeLevel FROM tblCourses ORDER BY courseName";

    private static final String ENROLL_STUDENT =
            "INSERT INTO tblStudentCourses(studentId, courseId) VALUES (?, ?)";

    private static final String SELECT_STUDENTS_BY_COURSE =
            "SELECT s.studentId, s.firstName, s.lastName, s.gradeLevel " +
            "FROM tblStudents s " +
            "JOIN tblStudentCourses sc ON s.studentId = sc.studentId " +
            "WHERE sc.courseId = ? " +
            "ORDER BY s.lastName";

    private static final String DELETE_ENROLLMENT =
            "DELETE FROM tblStudentCourses WHERE studentId = ? AND courseId = ?";

    // Create the link table if it does not exist
    private static final String CREATE_ENROLL_TABLE =
            "CREATE TABLE tblStudentCourses (" +
            "  studentId INTEGER NOT NULL, " +
            "  courseId  INTEGER NOT NULL, " +
            "  PRIMARY KEY (studentId, courseId), " +
            "  FOREIGN KEY (studentId) REFERENCES tblStudents(studentId), " +
            "  FOREIGN KEY (courseId)  REFERENCES tblCourses(courseId)" +
            ")";

    private static final String DELETE_ATTENDANCE =
            "DELETE FROM tblAttendance WHERE studentId=?";
    private static final String DELETE_GRADES =
            "DELETE FROM tblGrades WHERE studentId=?";
    private static final String DELETE_FINAL_GRADES =
            "DELETE FROM tblFinalGrades WHERE studentId=?";
    private static final String DELETE_FEEDBACK =
            "DELETE FROM tblFeedback WHERE studentId=?";

    /** Add a new student and return it with the new ID. */
    @Override
    public Student add(Student s) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(INSERT_STUDENT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.executeUpdate();

            // read back the generated id
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setStudentId(keys.getInt(1));
                }
            }
            return s;
        }
    }

    /** Update one student. */
    @Override
    public boolean update(Student s) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(UPDATE_STUDENT)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt(3, s.getGradeLevel());
            ps.setInt(4, s.getStudentId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Delete a student and all dependent records (attendance, grades, finalGrades,
     * feedback, enrolments). Done in a single transaction so we never leave the DB
     * in a half-deleted state.
     */
    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement psAttend  = c.prepareStatement(DELETE_ATTENDANCE);
             PreparedStatement psGrades  = c.prepareStatement(DELETE_GRADES);
             PreparedStatement psFinal   = c.prepareStatement(DELETE_FINAL_GRADES);
             PreparedStatement psNotes   = c.prepareStatement(DELETE_FEEDBACK);
             PreparedStatement psEnroll  = c.prepareStatement(DELETE_ENROLLMENTS);
             PreparedStatement psStudent = c.prepareStatement(DELETE_STUDENT)) {

            ensureEnrollmentTableExists(c);

            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // child tables first
                psAttend.setInt(1, id);  psAttend.executeUpdate();
                psGrades.setInt(1, id);  psGrades.executeUpdate();
                psFinal.setInt(1, id);   psFinal.executeUpdate();
                psNotes.setInt(1, id);   psNotes.executeUpdate();
                psEnroll.setInt(1, id);  psEnroll.executeUpdate();

                // then delete the student (parent)
                psStudent.setInt(1, id);
                int rows = psStudent.executeUpdate();

                c.commit();
                c.setAutoCommit(oldAuto);
                return rows == 1;
            } catch (SQLException ex) {
                c.rollback();
                c.setAutoCommit(oldAuto);
                throw ex;
            }
        }
    }

    /** Find one student by ID (or null). */
    @Override
    public Student findById(int id) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_STUDENT_BY_ID)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        }
    }

    /** List all students (sorted by last name). */
    @Override
    public List<Student> findAll() throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL_STUDENTS);
             ResultSet rs = ps.executeQuery()) {

            List<Student> list = new ArrayList<Student>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    /** List students in one grade level. */
    @Override
    public List<Student> findByGradeLevel(int gradeLevel) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_STUDENTS_BY_GRADE)) {

            ps.setInt(1, gradeLevel);
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<Student>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    /** List distinct grade levels that exist. */
    @Override
    public List<Integer> findGradeLevels() throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_GRADE_LEVELS);
             ResultSet rs = ps.executeQuery()) {

            List<Integer> grades = new ArrayList<Integer>();
            while (rs.next()) {
                grades.add(Integer.valueOf(rs.getInt("gradeLevel")));
            }
            return grades;
        }
    }

    /** List all courses (code + name). */
    @Override
    public List<Course> findCourses() throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_COURSES);
             ResultSet rs = ps.executeQuery()) {

            List<Course> courses = new ArrayList<Course>();
            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("courseId"),
                        rs.getString("courseCode"),
                        rs.getString("courseName"),
                        rs.getInt("gradeLevel")
                ));
            }
            return courses;
        }
    }

    /** Enrol a student in a course. */
    @Override
    public boolean enrollStudentInCourse(int studentId, int courseId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(ENROLL_STUDENT)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    /** List students enrolled in a course. */
    @Override
    public List<Student> findByCourse(int courseId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_STUDENTS_BY_COURSE)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Student> list = new ArrayList<Student>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }
        }
    }

    /** Unenrol a student from a course. */
    @Override
    public boolean removeStudentFromCourse(int studentId, int courseId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(DELETE_ENROLLMENT)) {

            ensureEnrollmentTableExists(c);
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    // --- helpers ---

    /** Build a Student from a row. */
    private static Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("studentId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getInt("gradeLevel")
        );
    }

    /**
     * Make sure the link table exists.
     * We check the metadata first; if missing, we create it.
     */
    private static void ensureEnrollmentTableExists(Connection c) throws SQLException {
        if (hasTable(c, "tblStudentCourses")) {
            return; // already there
        }
        try (Statement stmt = c.createStatement()) {
            stmt.executeUpdate(CREATE_ENROLL_TABLE);
        }
    }

    /** Check if a table name exists (case-insensitive). */
    private static boolean hasTable(Connection c, String tableName) throws SQLException {
        DatabaseMetaData meta = c.getMetaData();
        ResultSet rs = null;
        try {
            rs = meta.getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (name != null && name.equalsIgnoreCase(tableName)) {
                    return true;
                }
            }
            return false;
        } finally {
            // close ResultSet explicitly (not try-with-resources because of older drivers)
            if (rs != null) {
                try { rs.close(); } catch (SQLException ignore) { }
            }
        }
    }
}
