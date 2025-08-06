package sgms.ui;

import sgms.model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table model that displays all students with a selectable checkbox column.
 */
public class StudentSelectionTableModel extends AbstractTableModel {

    private final String[] columns = {"Select", "ID", "First Name", "Last Name", "Grade"};
    private final List<Student> students;
    private final Set<Integer> selectedIds;
    private final Set<Integer> originalIds;

    public StudentSelectionTableModel(List<Student> students, Set<Integer> initiallySelected) {
        this.students = new ArrayList<>(students);
        this.selectedIds = new HashSet<>(initiallySelected);
        this.originalIds = new HashSet<>(initiallySelected);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? Boolean.class : Object.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> selectedIds.contains(s.getStudentId());
            case 1 -> s.getStudentId();
            case 2 -> s.getFirstName();
            case 3 -> s.getLastName();
            case 4 -> s.getGradeLevel();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            Student s = students.get(rowIndex);
            boolean val = Boolean.TRUE.equals(aValue);
            if (val) {
                selectedIds.add(s.getStudentId());
            } else {
                selectedIds.remove(s.getStudentId());
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    public Set<Integer> getSelectedStudentIds() {
        return new HashSet<>(selectedIds);
    }

    /** Returns the set of student IDs that were selected when the model was created. */
    public Set<Integer> getOriginallySelectedIds() {
        return new HashSet<>(originalIds);
    }

    /** Returns true if the student at the given row was not originally selected. */
    public boolean isNewlySelected(int rowIndex) {
        Student s = students.get(rowIndex);
        int id = s.getStudentId();
        return selectedIds.contains(id) && !originalIds.contains(id);
    }

    /** Returns true if the student at the given row was deselected from the original set. */
    public boolean isDeselected(int rowIndex) {
        Student s = students.get(rowIndex);
        int id = s.getStudentId();
        return !selectedIds.contains(id) && originalIds.contains(id);
    }
}