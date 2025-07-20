package sgms.dao;

import sgms.dao.impl.UcanaccessStudentDAO;
import sgms.model.Student;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class StudentDAOTest {

    private static StudentDAO dao;

    @BeforeClass
    public static void setup() {           // JUnit-4 annotation + public static
        dao = new UcanaccessStudentDAO();
    }

    @Test
    public void addFindDeleteRoundTrip() throws Exception {
        // ── INSERT ────────────────────────────────────────────────
        Student s = new Student("Test", "User", 10);
        s = dao.add(s);
        assertTrue("ID should be generated", s.getStudentId() > 0);

        // ── READ ─────────────────────────────────────────────────
        Student found = dao.findById(s.getStudentId()).orElse(null);
        assertNotNull("Student must be found", found);
        assertEquals("User", found.getLastName());

        // ── DELETE ───────────────────────────────────────────────
        assertTrue("Row should delete", dao.delete(s.getStudentId()));
        assertFalse("Student should be gone",
                    dao.findById(s.getStudentId()).isPresent());
    }
}