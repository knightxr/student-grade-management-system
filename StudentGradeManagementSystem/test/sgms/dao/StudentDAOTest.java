package sgms.dao;

import sgms.dao.impl.UcanaccessStudentDAO;
import sgms.model.Student;
import sgms.util.DBManager;
import java.sql.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class StudentDAOTest {

    private static StudentDAO dao;

    @BeforeClass
    public static void setup() throws Exception {
        dao = new UcanaccessStudentDAO();
        try (Connection c = DBManager.get(); Statement st = c.createStatement()) {
            try {
                st.execute("ALTER TABLE tblCourses ADD COLUMN gradeLevel INTEGER DEFAULT 10");
            } catch (SQLException ignored) {
            }
        }
        dao = new UcanaccessStudentDAO();
    }

    @Test
    public void addFindDeleteRoundTrip() throws Exception {
        // ── INSERT ────────────────────────────────────────────────
        Student s = new Student("Test", "User", 10);
        s = dao.add(s);
        assertTrue("ID should be generated", s.getStudentId() > 0);

        // ── READ ─────────────────────────────────────────────────
        Student found = dao.findById(s.getStudentId());
        assertNotNull("Student must be found", found);
        assertEquals("User", found.getLastName());

        // ── DELETE ───────────────────────────────────────────────
        assertTrue("Row should delete", dao.delete(s.getStudentId()));
        assertNull("Student should be gone", dao.findById(s.getStudentId()));
    }

    @Test
    public void enrollStudentInCourse() throws Exception {
        // Create student
        Student s = dao.add(new Student("Course", "Tester", 9));

        int courseId;
        String code = "TST" + System.currentTimeMillis();
        // Insert course directly
        try (Connection c = DBManager.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO tblCourses(courseCode, courseName, gradeLevel) VALUES (?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, "Test Course");
            ps.setInt(3, 10);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                courseId = keys.getInt(1);
            }
        }

        // Enroll
        assertTrue(dao.enrollStudentInCourse(s.getStudentId(), courseId));

        // Verify
        assertTrue(dao.findByCourse(courseId).stream()
                .anyMatch(st -> st.getStudentId() == s.getStudentId()));

        // Remove enrollment
        assertTrue(dao.removeStudentFromCourse(s.getStudentId(), courseId));
        assertFalse(dao.findByCourse(courseId).stream()
                .anyMatch(st -> st.getStudentId() == s.getStudentId()));

        // Cleanup
        try (Connection c = DBManager.get(); Statement st = c.createStatement()) {
            st.executeUpdate("DELETE FROM tblStudentCourses WHERE studentId=" + s.getStudentId()
                    + " AND courseId=" + courseId);
            st.executeUpdate("DELETE FROM tblCourses WHERE courseId=" + courseId);
        }
        dao.delete(s.getStudentId());
    }
}