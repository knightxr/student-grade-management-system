package sgms.dao.impl;

import sgms.dao.CourseDAO;
import sgms.dao.DB;
import sgms.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Course DAO that uses UCanAccess (MS Access).
 * SQL uses parameters and try-with-resources.
 */
public class UcanaccessCourseDAO implements CourseDAO {

    // Normal string (no text block)
    private static final String SELECT_BY_GRADE =
            "SELECT c.courseId, c.courseCode, c.courseName, c.gradeLevel, " +
            "COUNT(sc.studentId) AS studentCount " +
            "FROM tblCourses c LEFT JOIN tblStudentCourses sc ON c.courseId = sc.courseId " +
            "WHERE c.gradeLevel = ? " +
            "GROUP BY c.courseId, c.courseCode, c.courseName, c.gradeLevel " +
            "ORDER BY c.courseName";

    private static final String INSERT_COURSE =
            "INSERT INTO tblCourses(courseCode, courseName, gradeLevel) VALUES (?, ?, ?)";

    private static final String UPDATE_COURSE =
            "UPDATE tblCourses SET courseName = ? WHERE courseId = ?";

    private static final String DELETE_COURSE =
            "DELETE FROM tblCourses WHERE courseId = ?";

    private static final String FIND_BY_CODE =
            "SELECT courseId, courseCode, courseName, gradeLevel FROM tblCourses WHERE courseCode = ?";

    /**
     * Get all courses for one grade level (with student counts).
     */
    @Override
    public List<Course> findByGrade(int gradeLevel) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_GRADE)) {

            ps.setInt(1, gradeLevel);

            try (ResultSet rs = ps.executeQuery()) {
                List<Course> list = new ArrayList<Course>();
                while (rs.next()) {
                    list.add(new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseCode"),
                            rs.getString("courseName"),
                            rs.getInt("gradeLevel"),
                            rs.getInt("studentCount")
                    ));
                }
                return list;
            }
        }
    }

    /**
     * Add a new course and return it with the new ID.
     */
    @Override
    public Course add(Course c) throws SQLException {
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(INSERT_COURSE, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setInt(3, c.getGradeLevel());
            ps.executeUpdate();

            // read back the generated ID
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCourseId(keys.getInt(1));
                }
            }
            return c;
        }
    }

    /**
     * Update the course name for an existing course.
     *
     * @return true if a row changed
     */
    @Override
    public boolean update(Course c) throws SQLException {
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(UPDATE_COURSE)) {

            ps.setString(1, c.getCourseName());
            ps.setInt(2, c.getCourseId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Delete one course by ID.
     *
     * @return true if a row was deleted
     */
    @Override
    public boolean delete(int courseId) throws SQLException {
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(DELETE_COURSE)) {

            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Find one course by its code.
     *
     * @return the course or null if not found
     */
    @Override
    public Course findByCode(String code) throws SQLException {
        try (Connection conn = DB.get();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_CODE)) {

            ps.setString(1, code);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseCode"),
                            rs.getString("courseName"),
                            rs.getInt("gradeLevel"),
                            0 // studentCount not needed here
                    );
                }
            }
        }
        return null;
    }
}
