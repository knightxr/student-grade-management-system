package sgms.dao.impl;

import sgms.dao.CourseDAO;
import sgms.model.Course;
import sgms.dao.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UCanAccess implementation of CourseDAO.
 */
public class UcanaccessCourseDAO implements CourseDAO {

    @Override
    public List<Course> findByGrade(int gradeLevel) throws SQLException {
        final String sql = "SELECT c.courseId, c.courseCode, c.courseName, c.gradeLevel, " +
                "COUNT(sc.studentId) AS studentCount " +
                "FROM tblCourses c LEFT JOIN tblStudentCourses sc ON c.courseId = sc.courseId " +
                "WHERE c.gradeLevel = ? " +
                "GROUP BY c.courseId, c.courseCode, c.courseName, c.gradeLevel " +
                "ORDER BY c.courseName";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gradeLevel);
            try (ResultSet rs = ps.executeQuery()) {
                List<Course> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseCode"),
                            rs.getString("courseName"),
                            rs.getInt("gradeLevel"),
                            rs.getInt("studentCount")));
                }
                return list;
            }
        }
    }

    @Override
    public Course add(Course c) throws SQLException {
        final String sql = "INSERT INTO tblCourses(courseCode, courseName, gradeLevel) VALUES (?, ?, ?)";
        try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getCourseCode());
            ps.setString(2, c.getCourseName());
            ps.setInt(3, c.getGradeLevel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setCourseId(keys.getInt(1));
                }
            }
            return c;
        }
    }

    @Override
    public boolean update(Course c) throws SQLException {
        final String sql = "UPDATE tblCourses SET courseName = ? WHERE courseId = ?";
        try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCourseName());
            ps.setInt(2, c.getCourseId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int courseId) throws SQLException {
        final String sql = "DELETE FROM tblCourses WHERE courseId = ?";
        try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Course findByCode(String code) throws SQLException {
        final String sql = "SELECT courseId, courseCode, courseName, gradeLevel FROM tblCourses WHERE courseCode = ?";
        try (Connection conn = Db.get(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getInt("courseId"),
                            rs.getString("courseCode"),
                            rs.getString("courseName"),
                            rs.getInt("gradeLevel"),
                            0);
                }
            }
        }
        return null;
    }
}