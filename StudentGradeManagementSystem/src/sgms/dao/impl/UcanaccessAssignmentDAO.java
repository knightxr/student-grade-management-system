package sgms.dao.impl;

import sgms.dao.AssignmentDAO;
import sgms.model.Assignment;
import sgms.util.DBManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment DAO backed by UCanAccess.
 */
public class UcanaccessAssignmentDAO implements AssignmentDAO {

    @Override
    public Assignment add(Assignment a) throws SQLException {
        final String sql = "INSERT INTO tblAssignments(courseId, title, maxMarks, term, dueDate) VALUES (?,?,?,?,?)";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getCourseId());
            ps.setString(2, a.getTitle());
            if (a.getMaxMarks() != null) {
                ps.setInt(3, a.getMaxMarks());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, a.getTerm());
            ps.setDate(5, a.getDueDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setAssignmentId(keys.getInt(1));
                }
            }
            return a;
        }
    }

    @Override
    public void update(Assignment a) throws SQLException {
        final String sql = "UPDATE tblAssignments SET title=?, maxMarks=?, term=?, dueDate=? WHERE assignmentId=?";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            if (a.getMaxMarks() != null) {
                ps.setInt(2, a.getMaxMarks());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, a.getTerm());
            ps.setDate(4, a.getDueDate());
            ps.setInt(5, a.getAssignmentId());
            ps.executeUpdate();
        }
    }
    
    @Override
    public boolean delete(int assignmentId) throws SQLException {
        final String delGrades = "DELETE FROM tblGrades WHERE assignmentId = ?";
        final String delAssign = "DELETE FROM tblAssignments WHERE assignmentId = ?";
        try (Connection c = DBManager.get();
             PreparedStatement psGrades = c.prepareStatement(delGrades);
             PreparedStatement psAssign = c.prepareStatement(delAssign)) {
            psGrades.setInt(1, assignmentId);
            psGrades.executeUpdate();
            psAssign.setInt(1, assignmentId);
            return psAssign.executeUpdate() == 1;
        }
    }

    @Override
    public List<Assignment> findByCourse(int courseId) throws SQLException {
        final String sql = "SELECT assignmentId, courseId, title, maxMarks, term, dueDate FROM tblAssignments WHERE courseId = ? ORDER BY assignmentId";
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Assignment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Assignment(
                            rs.getInt("assignmentId"),
                            rs.getInt("courseId"),
                            rs.getString("title"),
                            (Integer) rs.getObject("maxMarks"),
                            rs.getInt("term"),
                            rs.getDate("dueDate")
                    ));
                }
                return list;
            }
        }
    }
}