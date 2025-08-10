package sgms.dao.impl;

import sgms.dao.FinalGradeDAO;
import sgms.model.FinalGrade;
import sgms.util.DBManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for final grades backed by UCanAccess.
 */
public class UcanaccessFinalGradeDAO implements FinalGradeDAO {

    @Override
    public List<FinalGrade> findByGradeLevel(int gradeLevel) throws SQLException {
        final String sql = """
            SELECT s.firstName, s.lastName, f.term1, f.term2, f.term3, f.term4, f.finalGrade
            FROM tblFinalGrades f
            JOIN tblStudents s ON f.studentId = s.studentId
            WHERE s.gradeLevel = ?
            ORDER BY s.lastName
        """;
        try (Connection c = DBManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, gradeLevel);
            try (ResultSet rs = ps.executeQuery()) {
                List<FinalGrade> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new FinalGrade(
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            (Integer) rs.getObject("term1"),
                            (Integer) rs.getObject("term2"),
                            (Integer) rs.getObject("term3"),
                            (Integer) rs.getObject("term4"),
                            (Integer) rs.getObject("finalGrade")
                    ));
                }
                return list;
            }
        }
    }
}