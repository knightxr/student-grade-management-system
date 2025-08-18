package sgms.dao.impl;

import sgms.dao.DB;
import sgms.dao.GradeDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Grades DAO using UCanAccess (MS Access).
 * Stores raw marks per student and assignment.
 * Uses parameterised SQL and try-with-resources.
 */
public class UcanaccessGradeDAO implements GradeDAO {

    // Plain constant string (no text block)
    private static final String SELECT_BY_COURSE =
            "SELECT g.studentId, g.assignmentId, g.markAwarded " +
            "FROM tblGrades g " +
            "JOIN tblAssignments a ON g.assignmentId = a.assignmentId " +
            "WHERE a.courseId = ?";

    private static final String UPDATE_GRADE =
            "UPDATE tblGrades SET markAwarded = ? WHERE studentId = ? AND assignmentId = ?";

    private static final String INSERT_GRADE =
            "INSERT INTO tblGrades(studentId, assignmentId, markAwarded) VALUES (?,?,?)";

    /**
     * Get all grades for one course.
     * Map layout: studentId -> (assignmentId -> raw mark).
     */
    @Override
    public Map<Integer, Map<Integer, Integer>> findByCourse(int courseId) throws SQLException {
        Map<Integer, Map<Integer, Integer>> map = new HashMap<Integer, Map<Integer, Integer>>();

        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_COURSE)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("studentId");
                    int assignmentId = rs.getInt("assignmentId");
                    int mark = rs.getInt("markAwarded");

                    // No computeIfAbsent/lambdas: make the inner map if missing
                    Map<Integer, Integer> perStudent = map.get(Integer.valueOf(studentId));
                    if (perStudent == null) {
                        perStudent = new HashMap<Integer, Integer>();
                        map.put(Integer.valueOf(studentId), perStudent);
                    }
                    perStudent.put(Integer.valueOf(assignmentId), Integer.valueOf(mark));
                }
            }
        }
        return map;
    }

    /**
     * Add or update one grade for a student and assignment.
     * First try update; if no row changes, insert a new row.
     */
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
