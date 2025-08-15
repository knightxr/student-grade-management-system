package sgms.dao.impl;

import sgms.dao.FeedbackDAO;
import sgms.dao.Db;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/** Feedback DAO backed by UCanAccess. */
public class UcanaccessFeedbackDAO implements FeedbackDAO {

    @Override
    public Map<Integer, String> findByCourse(int courseId) throws SQLException {
        final String sql = "SELECT studentId, note FROM tblFeedback WHERE courseId = ?";
        Map<Integer, String> map = new HashMap<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("studentId"), rs.getString("note"));
                }
            }
        }
        return map;
    }

    @Override
    public void upsert(int studentId, int courseId, String note) throws SQLException {
        final String update = "UPDATE tblFeedback SET note = ?, entryDate = DATE() WHERE studentId = ? AND courseId = ?";
        final String insert = "INSERT INTO tblFeedback(studentId, courseId, note) VALUES (?,?,?)";
        try (Connection c = Db.get();
             PreparedStatement psUpdate = c.prepareStatement(update);
             PreparedStatement psInsert = c.prepareStatement(insert)) {
            psUpdate.setString(1, note);
            psUpdate.setInt(2, studentId);
            psUpdate.setInt(3, courseId);
            if (psUpdate.executeUpdate() == 0) {
                psInsert.setInt(1, studentId);
                psInsert.setInt(2, courseId);
                psInsert.setString(3, note);
                psInsert.executeUpdate();
            }
        }
    }

    @Override
    public void delete(int studentId, int courseId) throws SQLException {
        final String sql = "DELETE FROM tblFeedback WHERE studentId = ? AND courseId = ?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }
}