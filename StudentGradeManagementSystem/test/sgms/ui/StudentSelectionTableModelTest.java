package sgms.ui;

import org.junit.Test;
import sgms.model.Student;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Verifies tracking of newly added and removed enrollments in the selection model.
 */
public class StudentSelectionTableModelTest {

    @Test
    public void detectsEnrollmentChanges() {
        Student s1 = new Student("A", "A", 1); s1.setStudentId(1);
        Student s2 = new Student("B", "B", 2); s2.setStudentId(2);

        Set<Integer> initial = new HashSet<>();
        initial.add(1);                 // only s1 initially enrolled

        StudentSelectionTableModel model =
                new StudentSelectionTableModel(Arrays.asList(s1, s2), initial);

        assertEquals(initial, model.getOriginallySelectedIds());

        assertFalse(model.isNewlySelected(0));
        assertFalse(model.isDeselected(0));
        assertFalse(model.isNewlySelected(1));
        assertFalse(model.isDeselected(1));

        // select s2 -> newly selected
        model.setValueAt(Boolean.TRUE, 1, 0);
        assertTrue(model.isNewlySelected(1));

        // deselect s1 -> removed
        model.setValueAt(Boolean.FALSE, 0, 0);
        assertTrue(model.isDeselected(0));
    }
}