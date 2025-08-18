package sgms.dao.impl;

import sgms.dao.DB;
import sgms.dao.FeedbackDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Feedback DAO using UCanAccess (MS Access).
 * Uses parameterised SQL and try-with-resources.
 */
public class UcanaccessFeedbackDAO implements FeedbackDAO {

    // Fixed SQL text (no string building with user input)
    private static final String SELECT_FEEDBACK_BY_COURSE =
            "SELECT studentId, note FROM tblFeedback WHERE courseId = ?";

    private static final String UPDATE_FEEDBACK =
            "UPDATE tblFeedback SET note = ?, entryDate = DATE() " +
            "WHERE studentId = ? AND courseId = ?";

    private static final String INSERT_FEEDBACK =
            "INSERT INTO tblFeedback(studentId, courseId, note) VALUES (?,?,?)";

    private static final String DELETE_FEEDBACK =
            "DELETE FROM tblFeedback WHERE studentId = ? AND courseId = ?";

    /**
     * Get all feedback notes for one course.
     * Map layout: studentId -> note text.
     */
    @Override
    public Map<Integer, String> findByCourse(int courseId) throws SQLException {
        Map<Integer, String> map = new HashMap<Integer, String>();

        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_FEEDBACK_BY_COURSE)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("studentId");
                    String note = rs.getString("note");
                    map.put(Integer.valueOf(studentId), note);
                }
            }
        }
        return map;
    }

    /**
     * Add a new note or update an existing one for a student in a course.
     * First try update; if no row changes, insert a new row.
     */
    @Override
    public void upsert(int studentId, int courseId, String note) throws SQLException {
        try (Connection c = DB.get();
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

    /**
     * Remove a student's note for a course.
     */
    @Override
    public void delete(int studentId, int courseId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(DELETE_FEEDBACK)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }
}
