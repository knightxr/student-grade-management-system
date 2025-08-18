package sgms.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO providing reporting queries backed by UCanAccess.
 */
public class UcanaccessReportDAO {

    /**
     * Executes the term report join for the given term.
     */
    public ResultSet queryTermReport(Connection c, int term) throws SQLException {
        final String sql = """
        SELECT s.studentId, s.firstName, s.lastName,
        c.courseCode,
        a.title, a.maxMarks,
        g.markAwarded AS mark,
        IIF(a.maxMarks > 0, (g.markAwarded * 100.0) / a.maxMarks, NULL) AS pct
        FROM ((tblStudents AS s
        INNER JOIN tblStudentCourses AS sc ON s.studentId = sc.studentId)
        INNER JOIN tblCourses AS c ON sc.courseId = c.courseId)
        INNER JOIN (tblAssignments AS a
        LEFT JOIN tblGrades AS g
        ON a.assignmentId = g.assignmentId AND g.studentId = s.studentId)
        ON a.courseId = c.courseId
        WHERE a.term = ?
        ORDER BY s.lastName, c.courseCode, a.dueDate;
        """;
        PreparedStatement ps = c.prepareStatement(sql);
        ps.setInt(1, term);
        return ps.executeQuery();
    }
}
