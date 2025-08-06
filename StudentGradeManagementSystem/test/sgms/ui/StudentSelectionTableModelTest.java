package sgms.ui;

import org.junit.Test;
import sgms.model.Student;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;
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

    @Test
    public void sortsByGradeThenLastName() {
        Student s1 = new Student("F", "Smith", 10); s1.setStudentId(1);
        Student s2 = new Student("D", "Brown", 9);  s2.setStudentId(2);
        Student s3 = new Student("E", "Aaron", 9);  s3.setStudentId(3);
        Student s4 = new Student("C", "Adams", 10); s4.setStudentId(4);

        List<Student> students = Arrays.asList(s1, s2, s3, s4);
        StudentSelectionTableModel model = new StudentSelectionTableModel(students, Set.of());

        JTable table = new JTable(model);
        TableRowSorter<StudentSelectionTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        sorter.setSortKeys(List.of(
                new RowSorter.SortKey(4, SortOrder.ASCENDING),
                new RowSorter.SortKey(3, SortOrder.ASCENDING),
                new RowSorter.SortKey(2, SortOrder.ASCENDING)));

        List<Student> actualOrder = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelIndex = table.convertRowIndexToModel(i);
            actualOrder.add(students.get(modelIndex));
        }

        assertEquals(Arrays.asList(s3, s2, s4, s1), actualOrder);
    }
}