package sgms.dao.impl;

import sgms.dao.AttendanceDAO;
import sgms.dao.Db;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Attendance DAO backed by UCanAccess.
 */
public class UcanaccessAttendanceDAO implements AttendanceDAO {

    @Override
    public Map<Integer, Map<LocalDate, Boolean>> findByCourseAndDateRange(int courseId, LocalDate start, LocalDate end) throws SQLException {
        final String sql = """
            SELECT studentId, attendanceDate, present
            FROM tblAttendance
            WHERE courseId = ? AND attendanceDate BETWEEN ? AND ?
        """;
        Map<Integer, Map<LocalDate, Boolean>> map = new HashMap<>();
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("studentId");
                    LocalDate date = rs.getDate("attendanceDate").toLocalDate();
                    boolean present = rs.getBoolean("present");
                    map.computeIfAbsent(studentId, k -> new HashMap<>()).put(date, present);
                }
            }
        }
        return map;
    }

    @Override
    public void upsert(int studentId, int courseId, LocalDate date, boolean present) throws SQLException {
        final String update = "UPDATE tblAttendance SET present=? WHERE studentId=? AND courseId=? AND attendanceDate=?";
        final String insert = "INSERT INTO tblAttendance(studentId, courseId, attendanceDate, present) VALUES (?,?,?,?)";
        try (Connection c = Db.get();
             PreparedStatement psUpdate = c.prepareStatement(update);
             PreparedStatement psInsert = c.prepareStatement(insert)) {
            psUpdate.setBoolean(1, present);
            psUpdate.setInt(2, studentId);
            psUpdate.setInt(3, courseId);
            psUpdate.setDate(4, Date.valueOf(date));
            if (psUpdate.executeUpdate() == 0) {
                psInsert.setInt(1, studentId);
                psInsert.setInt(2, courseId);
                psInsert.setDate(3, Date.valueOf(date));
                psInsert.setBoolean(4, present);
                psInsert.executeUpdate();
            }
        }
    }
}