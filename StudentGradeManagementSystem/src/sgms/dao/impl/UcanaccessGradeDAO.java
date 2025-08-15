package sgms.dao.impl;

import sgms.dao.GradeDAO;
import sgms.dao.DB;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Grade DAO backed by UCanAccess.
 */
public class UcanaccessGradeDAO implements GradeDAO {

    private static final String SELECT_BY_COURSE = """
            SELECT g.studentId, g.assignmentId, g.markAwarded
            FROM tblGrades g
            JOIN tblAssignments a ON g.assignmentId = a.assignmentId
            WHERE a.courseId = ?
            """;
    private static final String UPDATE_GRADE =
            "UPDATE tblGrades SET markAwarded = ? WHERE studentId = ? AND assignmentId = ?";
    private static final String INSERT_GRADE =
            "INSERT INTO tblGrades(studentId, assignmentId, markAwarded) VALUES (?,?,?)";

    @Override
    public Map<Integer, Map<Integer, Integer>> findByCourse(int courseId) throws SQLException {
        Map<Integer, Map<Integer, Integer>> map = new HashMap<>();
        try (Connection c = DB.get(); PreparedStatement ps = c.prepareStatement(SELECT_BY_COURSE)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("studentId");
                    int assignmentId = rs.getInt("assignmentId");
                    int mark = rs.getInt("markAwarded");
                    map.computeIfAbsent(studentId, k -> new HashMap<>()).put(assignmentId, mark);
                }
            }
        }
        return map;
    }

    @Override
    public void upsert(int studentId, int assignmentId, int mark) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement psUpdate = c.prepareStatement(UPDATE_GRADE);
             PreparedStatement psInsert = c.prepareStatement(INSERT_GRADE)) {
            psUpdate.setInt(1, mark);
            psUpdate.setInt(2, studentId);
            psUpdate.setInt(3, assignmentId);
            if (psUpdate.executeUpdate() == 0) {
                psInsert.setInt(1, studentId);
                psInsert.setInt(2, assignmentId);
                psInsert.setInt(3, mark);
                psInsert.executeUpdate();
            }
        }
    }
}