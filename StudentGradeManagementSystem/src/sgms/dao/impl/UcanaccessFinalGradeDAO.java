package sgms.dao.impl;

import sgms.dao.FinalGradeDAO;
import sgms.model.FinalGrade;
import sgms.util.DBManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * DAO for final grades backed by UCanAccess.
 */
public class UcanaccessFinalGradeDAO implements FinalGradeDAO {

    @Override
    public List<FinalGrade> findByGradeLevel(int gradeLevel) throws SQLException {
        final String sql = """
            SELECT s.firstName, s.lastName,
                   AVG(CASE WHEN a.term = 1 AND g.markAwarded IS NOT NULL THEN g.markAwarded END) AS term1,
                   AVG(CASE WHEN a.term = 2 AND g.markAwarded IS NOT NULL THEN g.markAwarded END) AS term2,
                   AVG(CASE WHEN a.term = 3 AND g.markAwarded IS NOT NULL THEN g.markAwarded END) AS term3,
                   AVG(CASE WHEN a.term = 4 AND g.markAwarded IS NOT NULL THEN g.markAwarded END) AS term4
            FROM tblStudents s
            JOIN tblStudentCourses sc ON s.studentId = sc.studentId
            JOIN tblAssignments a ON sc.courseId = a.courseId
            LEFT JOIN tblGrades g ON g.studentId = s.studentId AND g.assignmentId = a.assignmentId
            WHERE s.gradeLevel = ?
            GROUP BY s.firstName, s.lastName, s.studentId
            ORDER BY s.lastName
        """;
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gradeLevel);
            try (ResultSet rs = ps.executeQuery()) {
                List<FinalGrade> list = new ArrayList<>();
                while (rs.next()) {
                    Double term1 = clamp(getDouble(rs, "term1"));
                    Double term2 = clamp(getDouble(rs, "term2"));
                    Double term3 = clamp(getDouble(rs, "term3"));
                    Double term4 = clamp(getDouble(rs, "term4"));
                    Integer finalGrade = null;
                    if (term1 != null || term2 != null || term3 != null || term4 != null) {
                        double total = (term1 != null ? term1 : 0) * 0.125
                                     + (term2 != null ? term2 : 0) * 0.25
                                     + (term3 != null ? term3 : 0) * 0.125
                                     + (term4 != null ? term4 : 0) * 0.5;
                        finalGrade = (int) Math.round(Math.min(total, 100));
                    }
                    list.add(new FinalGrade(
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            term1 != null ? (int) Math.round(term1) : null,
                            term2 != null ? (int) Math.round(term2) : null,
                            term3 != null ? (int) Math.round(term3) : null,
                            term4 != null ? (int) Math.round(term4) : null,
                            finalGrade
                    ));
                }
                return list;
            }
        }
    }
    
    @Override
    public List<Integer> findGradeLevels() throws SQLException {
        final String sql = "SELECT DISTINCT gradeLevel FROM tblStudents ORDER BY gradeLevel";
        try (Connection c = DBManager.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            List<Integer> grades = new ArrayList<>();
            while (rs.next()) {
                grades.add(rs.getInt(1));
            }
            return grades;
        }
    }

    /**
     * Helper to safely read a Double from a result set.
     */
    private Double getDouble(ResultSet rs, String column) throws SQLException {
        BigDecimal bd = rs.getBigDecimal(column);
        return bd != null ? bd.doubleValue() : null;
    }

    /**
     * Clamp a percentage value to the range 0-100.
     */
    private Double clamp(Double value) {
        return value != null ? Math.max(0.0, Math.min(value, 100.0)) : null;
    }
}