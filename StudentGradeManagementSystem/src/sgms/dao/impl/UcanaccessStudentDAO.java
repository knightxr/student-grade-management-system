package sgms.dao.impl;

import sgms.dao.StudentDAO;
import sgms.model.Student;
import sgms.util.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UCanAccess-backed implementation of StudentDAO.
 * Uses prepared statements for safety and speed.
 */
public class UcanaccessStudentDAO implements StudentDAO {

    /* ─────────────────────── CREATE ─────────────────────────────── */

    @Override
    public Student add(Student s) throws SQLException {
        final String sql = """
            INSERT INTO tblStudents(firstName, lastName, gradeLevel)
            VALUES (?,?,?)
        """;
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt   (3, s.getGradeLevel());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setStudentId(keys.getInt(1));
                }
            }
            return s;
        }
    }

    /* ─────────────────────── UPDATE ─────────────────────────────── */

    @Override
    public boolean update(Student s) throws SQLException {
        final String sql = """
            UPDATE tblStudents
            SET firstName = ?, lastName = ?, gradeLevel = ?
            WHERE studentId = ?
        """;
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setInt   (3, s.getGradeLevel());
            ps.setInt   (4, s.getStudentId());
            return ps.executeUpdate() == 1;
        }
    }

    /* ─────────────────────── DELETE ─────────────────────────────── */

    @Override
    public boolean delete(int id) throws SQLException {
        final String sql = "DELETE FROM tblStudents WHERE studentId = ?";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /* ─────────────────────── READ (single) ──────────────────────── */

    @Override
    public Optional<Student> findById(int id) throws SQLException {
        final String sql = "SELECT * FROM tblStudents WHERE studentId = ?";
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }

    /* ─────────────────────── READ (all) ─────────────────────────── */

    @Override
    public List<Student> findAll() throws SQLException {
        final String sql = "SELECT * FROM tblStudents ORDER BY lastName";
        try (Connection c = DBManager.get();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            List<Student> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    /* ─────────────────────── Helper ─────────────────────────────── */

    /** Converts the current row of a ResultSet into a Student object. */
    private Student map(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt   ("studentId"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getInt   ("gradeLevel")
        );
    }
}