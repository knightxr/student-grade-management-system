package sgms.dao.impl;

import sgms.dao.AssignmentDAO;
import sgms.dao.DB;
import sgms.model.Assignment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Assignment DAO that uses UCanAccess (MS Access).
 * All SQL uses parameters and try-with-resources.
 */
public class UcanaccessAssignmentDAO implements AssignmentDAO {

    // SQL statements (fixed text; no string building with user input)
    private static final String INSERT_ASSIGNMENT =
            "INSERT INTO tblAssignments(courseId, title, maxMarks, term, dueDate) VALUES (?,?,?,?,?)";

    private static final String UPDATE_ASSIGNMENT =
            "UPDATE tblAssignments SET title=?, maxMarks=?, term=?, dueDate=? WHERE assignmentId=?";

    private static final String DELETE_GRADES =
            "DELETE FROM tblGrades WHERE assignmentId = ?";

    private static final String DELETE_ASSIGNMENT =
            "DELETE FROM tblAssignments WHERE assignmentId = ?";

    private static final String SELECT_BY_COURSE =
            "SELECT assignmentId, courseId, title, maxMarks, term, dueDate " +
            "FROM tblAssignments WHERE courseId = ? ORDER BY assignmentId";

    /**
     * Add a new assignment and return it with the generated ID.
     */
    @Override
    public Assignment add(Assignment a) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(INSERT_ASSIGNMENT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getCourseId());
            ps.setString(2, a.getTitle());
            if (a.getMaxMarks() != null) {
                ps.setInt(3, a.getMaxMarks().intValue());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setInt(4, a.getTerm());
            ps.setDate(5, a.getDueDate());

            ps.executeUpdate();

            // read back the new ID
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setAssignmentId(keys.getInt(1));
                }
            }
            return a;
        }
    }

    /**
     * Update an existing assignment.
     */
    @Override
    public void update(Assignment a) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(UPDATE_ASSIGNMENT)) {

            ps.setString(1, a.getTitle());
            if (a.getMaxMarks() != null) {
                ps.setInt(2, a.getMaxMarks().intValue());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, a.getTerm());
            ps.setDate(4, a.getDueDate());
            ps.setInt(5, a.getAssignmentId());

            ps.executeUpdate();
        }
    }

    /**
     * Delete one assignment. Also removes its grades first.
     *
     * @return true if the assignment row was deleted
     */
    @Override
    public boolean delete(int assignmentId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement psGrades = c.prepareStatement(DELETE_GRADES);
             PreparedStatement psAssign = c.prepareStatement(DELETE_ASSIGNMENT)) {

            // remove child rows, then the assignment
            psGrades.setInt(1, assignmentId);
            psGrades.executeUpdate();

            psAssign.setInt(1, assignmentId);
            return psAssign.executeUpdate() == 1;
        }
    }

    /**
     * List all assignments for a course.
     */
    @Override
    public List<Assignment> findByCourse(int courseId) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_COURSE)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Assignment> list = new ArrayList<Assignment>();
                while (rs.next()) {
                    list.add(new Assignment(
                            rs.getInt("assignmentId"),
                            rs.getInt("courseId"),
                            rs.getString("title"),
                            (Integer) rs.getObject("maxMarks"), // may be null
                            rs.getInt("term"),
                            rs.getDate("dueDate")
                    ));
                }
                return list;
            }
        }
    }
}
