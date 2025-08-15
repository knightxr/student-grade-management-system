package sgms.ui;

import sgms.model.Assignment;
import sgms.model.Student;
import sgms.util.GradeCalculator;
import java.awt.HeadlessException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StudentGradesTableModelTest {

    private StudentGradesTableModel model;
    private Student s1;

    @Before
    public void setUp() {
        s1 = new Student("Alice", "Anderson", 12); s1.setStudentId(1);
        Student s2 = new Student("Bob", "Brown", 12); s2.setStudentId(2);
        List<Student> students = new ArrayList<Student>();
        students.add(s1);
        students.add(s2);

        Assignment a1 = new Assignment(1, 1, "A1", Integer.valueOf(70), 1, (Date) null);
        Assignment a2 = new Assignment(2, 1, "A2", Integer.valueOf(50), 2, (Date) null);
        Assignment a4 = new Assignment(3, 1, "A4", Integer.valueOf(100), 4, (Date) null);
        List<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(a1);
        assignments.add(a2);
        assignments.add(a4);

        Map<Integer, Integer> g1 = new HashMap<Integer, Integer>();
        g1.put(a1.getAssignmentId(), Integer.valueOf(35));
        g1.put(a2.getAssignmentId(), Integer.valueOf(40));
        g1.put(a4.getAssignmentId(), Integer.valueOf(80));
        Map<Integer, Map<Integer, Integer>> grades = new HashMap<Integer, Map<Integer, Integer>>();
        grades.put(s1.getStudentId(), g1);

        model = new StudentGradesTableModel(students, assignments, grades);
    }

    @Test
    public void getValueAtReturnsNamesAndMarks() {
        assertEquals("Alice Anderson", model.getValueAt(0, 0));
        assertEquals(35, model.getValueAt(0, 1));
        assertEquals(40, model.getValueAt(0, 2));
        assertEquals(80, model.getValueAt(0, 3));
    }

    @Test
    public void setValueAtRejectsAboveMax() {
        try {
            model.setValueAt(Integer.valueOf(60), 0, 2); // max is 50
        } catch (HeadlessException ex) {
            // ignore headless UI warning
        }
        assertEquals(40, model.getValueAt(0, 2));
    }

    @Test
    public void termAndFinalPercentagesComputed() {
        Double t1 = model.getTermAveragePercent(s1.getStudentId(), 1);
        Double t2 = model.getTermAveragePercent(s1.getStudentId(), 2);
        Double t4 = model.getTermAveragePercent(s1.getStudentId(), 4);
        assertEquals(50.0, t1.doubleValue(), 0.001);
        assertEquals(80.0, t2.doubleValue(), 0.001);
        assertEquals(80.0, t4.doubleValue(), 0.001);

        Double finalPct = model.getFinalPercent(s1.getStudentId());
        Double expected = GradeCalculator.calculateFinalGrade(t1, t2, null, t4);
        assertEquals(expected.doubleValue(), finalPct.doubleValue(), 0.001);
    }
}