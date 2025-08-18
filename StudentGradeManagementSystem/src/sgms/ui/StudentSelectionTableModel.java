package sgms.ui;

import sgms.model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table model with a select checkbox for each student.
 * Columns: Select | ID | First Name | Last Name | Grade
 */
public class StudentSelectionTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = { "Select", "ID", "First Name", "Last Name", "Grade" };

    private final List<Student> students;
    private final Set<Integer> selectedIds; // current selection
    private final Set<Integer> originalIds; // snapshot from construction

    public StudentSelectionTableModel(List<Student> students, Set<Integer> initiallySelected) {
        this.students     = new ArrayList<Student>(students);
        this.selectedIds  = new HashSet<Integer>(initiallySelected);
        this.originalIds  = new HashSet<Integer>(initiallySelected);
    }

    // Backwards-compatible constructor (DAO not used here)
    public StudentSelectionTableModel(List<Student> students,
                                      Set<Integer> initiallySelected,
                                      sgms.dao.StudentDAO ignored) {
        this(students, initiallySelected);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class; // Select
            case 1: return Integer.class; // ID
            case 4: return Integer.class; // Grade
            case 2: // First Name
            case 3: // Last Name
            default: return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0; // only the checkbox is editable
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return Boolean.valueOf(selectedIds.contains(Integer.valueOf(s.getStudentId())));
            case 1: return Integer.valueOf(s.getStudentId());
            case 2: return s.getFirstName();
            case 3: return s.getLastName();
            case 4: return Integer.valueOf(s.getGradeLevel());
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0) return;
        Student s = students.get(rowIndex);
        Integer id = Integer.valueOf(s.getStudentId());
        if (Boolean.TRUE.equals(aValue)) {
            selectedIds.add(id);
        } else {
            selectedIds.remove(id);
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /** Current selection as student IDs. */
    public Set<Integer> getSelectedStudentIds() {
        return new HashSet<Integer>(selectedIds);
    }

    /** The selection as it was when the model was created. */
    public Set<Integer> getOriginallySelectedIds() {
        return new HashSet<Integer>(originalIds);
    }

    /** True if this row is a new selection that wasn’t in the original set. */
    public boolean isNewlySelected(int rowIndex) {
        Student s = students.get(rowIndex);
        Integer id = Integer.valueOf(s.getStudentId());
        return selectedIds.contains(id) && !originalIds.contains(id);
    }

    /** True if this row was in the original set but is now deselected. */
    public boolean isDeselected(int rowIndex) {
        Student s = students.get(rowIndex);
        Integer id = Integer.valueOf(s.getStudentId());
        return !selectedIds.contains(id) && originalIds.contains(id);
    }
}
