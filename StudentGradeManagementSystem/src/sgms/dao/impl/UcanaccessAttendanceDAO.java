package sgms.dao.impl;

import sgms.dao.AttendanceDAO;
import sgms.dao.DB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Attendance DAO using UCanAccess (MS Access).
 * Uses parameterised SQL and try-with-resources.
 */
public class UcanaccessAttendanceDAO implements AttendanceDAO {

    // Normal string constant (no text block)
    private static final String SELECT_BY_COURSE_AND_RANGE =
            "SELECT studentId, attendanceDate, present " +
            "FROM tblAttendance " +
            "WHERE courseId = ? AND attendanceDate BETWEEN ? AND ?";

    private static final String UPDATE_ATTENDANCE =
            "UPDATE tblAttendance SET present=? WHERE studentId=? AND courseId=? AND attendanceDate=?";

    private static final String INSERT_ATTENDANCE =
            "INSERT INTO tblAttendance(studentId, courseId, attendanceDate, present) VALUES (?,?,?,?)";

    /**
     * Get attendance for all students in a course between two dates.
     * Map layout: studentId -> (date -> present).
     */
    @Override
    public Map<Integer, Map<LocalDate, Boolean>> findByCourseAndDateRange(
            int courseId, LocalDate start, LocalDate end) throws SQLException {

        Map<Integer, Map<LocalDate, Boolean>> map = new HashMap<Integer, Map<LocalDate, Boolean>>();

        try (Connection c = DB.get();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_COURSE_AND_RANGE)) {

            ps.setInt(1, courseId);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int studentId = rs.getInt("studentId");
                    LocalDate date = rs.getDate("attendanceDate").toLocalDate();
                    boolean present = rs.getBoolean("present");

                    // No lambdas: create inner map if missing
                    Map<LocalDate, Boolean> perDay = map.get(studentId);
                    if (perDay == null) {
                        perDay = new HashMap<LocalDate, Boolean>();
                        map.put(studentId, perDay);
                    }
                    perDay.put(date, Boolean.valueOf(present));
                }
            }
        }
        return map;
    }

    /**
     * Add or update one attendance record.
     * First try update; if no row changes, insert a new one.
     */
    @Override
    public void upsert(int studentId, int courseId, LocalDate date, boolean present) throws SQLException {
        try (Connection c = DB.get();
             PreparedStatement psUpdate = c.prepareStatement(UPDATE_ATTENDANCE);
             PreparedStatement psInsert = c.prepareStatement(INSERT_ATTENDANCE)) {

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
