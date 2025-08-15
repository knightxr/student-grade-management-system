package sgms.dao.impl;

import sgms.dao.FeedbackDAO;
import sgms.dao.Db;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/** Feedback DAO backed by UCanAccess. */
public class UcanaccessFeedbackDAO implements FeedbackDAO {

    private static final String SELECT_FEEDBACK_BY_COURSE =
            "SELECT studentId, note FROM tblFeedback WHERE courseId = ?";
    private static final String UPDATE_FEEDBACK =
            "UPDATE tblFeedback SET note = ?, entryDate = DATE() WHERE studentId = ? AND courseId = ?";
    private static final String INSERT_FEEDBACK =
            "INSERT INTO tblFeedback(studentId, courseId, note) VALUES (?,?,?)";
    private static final String DELETE_FEEDBACK =
            "DELETE FROM tblFeedback WHERE studentId = ? AND courseId = ?";

    @Override
    public Map<Integer, String> findByCourse(int courseId) throws SQLException {
        Map<Integer, String> map = new HashMap<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(SELECT_FEEDBACK_BY_COURSE)) {
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
        try (Connection c = Db.get();
             PreparedStatement psUpdate = c.prepareStatement(UPDATE_FEEDBACK);
             PreparedStatement psInsert = c.prepareStatement(INSERT_FEEDBACK)) {
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
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(DELETE_FEEDBACK)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }
}