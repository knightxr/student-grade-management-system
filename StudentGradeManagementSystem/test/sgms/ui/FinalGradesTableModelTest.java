package sgms.ui;

import sgms.model.Assignment;
import sgms.model.Student;
import sgms.util.GradeCalculator;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FinalGradesTableModelTest {

    private FinalGradesTableModel model;
    private Student s1;
    private StudentGradesTableModel backing;

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

        backing = new StudentGradesTableModel(students, assignments, grades);
        model = new FinalGradesTableModel(students, backing);
    }

    @Test
    public void hasCorrectColumnNames() {
        assertEquals("First Name", model.getColumnName(0));
        assertEquals("Last Name", model.getColumnName(1));
        assertEquals("T1", model.getColumnName(2));
        assertEquals("T2", model.getColumnName(3));
        assertEquals("T3", model.getColumnName(4));
        assertEquals("T4", model.getColumnName(5));
        assertEquals("Final %", model.getColumnName(6));
    }

    @Test
    public void returnsRoundedPercentages() {
        assertEquals("Alice", model.getValueAt(0, 0));
        assertEquals("Anderson", model.getValueAt(0, 1));
        assertEquals(Integer.valueOf(50), model.getValueAt(0, 2));
        assertEquals(Integer.valueOf(80), model.getValueAt(0, 3));
        assertNull(model.getValueAt(0, 4));
        assertEquals(Integer.valueOf(80), model.getValueAt(0, 5));
        Double expected = GradeCalculator.calculateFinalGrade(50.0, 80.0, null, 80.0);
        assertEquals(Integer.valueOf((int)Math.round(expected.doubleValue())), model.getValueAt(0, 6));
    }
}