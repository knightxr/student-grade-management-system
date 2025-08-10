package sgms.dao.impl;

import sgms.dao.GradeDAO;
import sgms.util.DBManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Grade DAO backed by UCanAccess.
 */
public class UcanaccessGradeDAO implements GradeDAO {

    @Override
    public Map<Integer, Map<Integer, Integer>> findByCourse(int courseId) throws SQLException {
        final String sql = """
            SELECT g.studentId, g.assignmentId, g.markAwarded
            FROM tblGrades g
            JOIN tblAssignments a ON g.assignmentId = a.assignmentId
            WHERE a.courseId = ?
        """;
        Map<Integer, Map<Integer, Integer>> map = new HashMap<>();
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
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
        final String update = "UPDATE tblGrades SET markAwarded = ? WHERE studentId = ? AND assignmentId = ?";
        final String insert = "INSERT INTO tblGrades(studentId, assignmentId, markAwarded) VALUES (?,?,?)";
        try (Connection c = DBManager.get();
             PreparedStatement psUpdate = c.prepareStatement(update);
             PreparedStatement psInsert = c.prepareStatement(insert)) {
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