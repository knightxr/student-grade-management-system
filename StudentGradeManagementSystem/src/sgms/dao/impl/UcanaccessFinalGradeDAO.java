package sgms.dao.impl;

import sgms.dao.DB;
import sgms.dao.FinalGradeDAO;
import sgms.model.FinalGrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Final grade DAO using UCanAccess (MS Access).
 * The SQL computes term averages as percentages: (raw / maxMarks) * 100.
 */
public class UcanaccessFinalGradeDAO implements FinalGradeDAO {

    // No text block; plain constant string.
    // Each term uses an AVG over a safe percentage:
    // IIF(maxMarks is null or 0, NULL, (markAwarded * 100.0) / maxMarks)
    private static final String SELECT_BY_GRADE_LEVEL =
            "SELECT s.firstName, s.lastName, " +
            "AVG(IIF(a.term = 1 AND g.markAwarded IS NOT NULL AND a.maxMarks IS NOT NULL AND a.maxMarks <> 0, (g.markAwarded * 100.0) / a.maxMarks, NULL)) AS term1, " +
            "AVG(IIF(a.term = 2 AND g.markAwarded IS NOT NULL AND a.maxMarks IS NOT NULL AND a.maxMarks <> 0, (g.markAwarded * 100.0) / a.maxMarks, NULL)) AS term2, " +
            "AVG(IIF(a.term = 3 AND g.markAwarded IS NOT NULL AND a.maxMarks IS NOT NULL AND a.maxMarks <> 0, (g.markAwarded * 100.0) / a.maxMarks, NULL)) AS term3, " +
            "AVG(IIF(a.term = 4 AND g.markAwarded IS NOT NULL AND a.maxMarks IS NOT NULL AND a.maxMarks <> 0, (g.markAwarded * 100.0) / a.maxMarks, NULL)) AS term4 " +
            "FROM tblStudents s " +
            "JOIN tblStudentCourses sc ON s.studentId = sc.studentId " +
            "JOIN tblAssignments a ON sc.courseId = a.courseId " +
            "LEFT JOIN tblGrades g ON g.studentId = s.studentId AND g.assignmentId = a.assignmentId " +
            "WHERE s.gradeLevel = ? " +
            "GROUP BY s.firstName, s.lastName, s.studentId " +
            "ORDER BY s.lastName";

    private static final String SELECT_GRADE_LEVELS =
            "SELECT DISTINCT gradeLevel FROM tblStudents ORDER BY gradeLevel";

    /**
     * Get final-grade rows for one grade level.
     * Term1..Term4 are rounded percentages; Final is weighted (12.5, 25, 12.5, 50).
     */
    @Override
    public List<FinalGrade> findByGradeLevel(int gradeLevel) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_GRADE_LEVEL)) {

            ps.setInt(1, gradeLevel);

            try (ResultSet rs = ps.executeQuery()) {
                List<FinalGrade> list = new ArrayList<FinalGrade>();
                while (rs.next()) {
                    Double t1 = getNullableDouble(rs, "term1");
                    Double t2 = getNullableDouble(rs, "term2");
                    Double t3 = getNullableDouble(rs, "term3");
                    Double t4 = getNullableDouble(rs, "term4");

                    // Clamp each term to [0,100] and round for display
                    Integer rt1 = t1 != null ? Integer.valueOf((int) Math.round(clamp01to100(t1.doubleValue()))) : null;
                    Integer rt2 = t2 != null ? Integer.valueOf((int) Math.round(clamp01to100(t2.doubleValue()))) : null;
                    Integer rt3 = t3 != null ? Integer.valueOf((int) Math.round(clamp01to100(t3.doubleValue()))) : null;
                    Integer rt4 = t4 != null ? Integer.valueOf((int) Math.round(clamp01to100(t4.doubleValue()))) : null;

                    // Weighted final: 12.5%, 25%, 12.5%, 50%, renormalised if some terms are missing
                    Double finalPct = weightedFinal(t1, t2, t3, t4);
                    Integer rFinal = finalPct != null ? Integer.valueOf((int) Math.round(clamp01to100(finalPct.doubleValue()))) : null;

                    list.add(new FinalGrade(
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rt1, rt2, rt3, rt4, rFinal
                    ));
                }
                return list;
            }
        }
    }

    /**
     * List the grade levels that exist.
     */
    @Override
    public List<Integer> findGradeLevels() throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_GRADE_LEVELS);
             ResultSet rs = ps.executeQuery()) {

            List<Integer> grades = new ArrayList<Integer>();
            while (rs.next()) {
                grades.add(Integer.valueOf(rs.getInt(1)));
            }
            return grades;
        }
    }

    // --- helpers ---

    /** Read a Double column that may be NULL. */
    private static Double getNullableDouble(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        if (rs.wasNull()) return null;
        return Double.valueOf(v);
    }

    /** Keep a percentage within 0..100. */
    private static double clamp01to100(double v) {
        if (v < 0.0) return 0.0;
        if (v > 100.0) return 100.0;
        return v;
    }

    /**
     * Weighted final for up to four terms (12.5, 25, 12.5, 50).
     * If some terms are null, renormalise using only the present terms.
     */
    private static Double weightedFinal(Double t1, Double t2, Double t3, Double t4) {
        double sum = 0.0;
        double w   = 0.0;

        if (t1 != null) { sum += t1.doubleValue() * 0.125; w += 0.125; }
        if (t2 != null) { sum += t2.doubleValue() * 0.25;  w += 0.25;  }
        if (t3 != null) { sum += t3.doubleValue() * 0.125; w += 0.125; }
        if (t4 != null) { sum += t4.doubleValue() * 0.50;  w += 0.50;  }

        if (w == 0.0) return null;
        return Double.valueOf(sum / w);
    }
}
