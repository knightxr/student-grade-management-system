package sgms.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import sgms.model.Student;

/**
 * Table model for the “View Students” screen.
 * Columns: Delete | ID | First Name | Last Name | Grade
 */
public class StudentTableModel extends AbstractTableModel {

    private final String[] columns = { "Delete", "ID", "First Name", "Last Name", "Grade" };
    private final List<Student> students;
    private final Set<Integer> deletedIds = new HashSet<Integer>();

    public StudentTableModel(List<Student> list) {
        this.students = (list == null) ? new ArrayList<Student>() : new ArrayList<Student>(list);
    }

    /** Older signature kept for compatibility (DAO not used here). */
    public StudentTableModel(List<Student> list, sgms.dao.StudentDAO ignored) {
        this(list);
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

    /** ID and Grade as Integer so sorting works as numbers. */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Boolean.class; // Delete
            case 1: return Integer.class; // ID
            case 2: return String.class;  // First Name
            case 3: return String.class;  // Last Name
            case 4: return Integer.class; // Grade
            default: return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Delete + First/Last/Grade are editable; ID is read-only
        return columnIndex == 0 || columnIndex > 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return deletedIds.contains(Integer.valueOf(s.getStudentId()));
            case 1: return Integer.valueOf(s.getStudentId());
            case 2: return s.getFirstName();
            case 3: return s.getLastName();
            case 4: return Integer.valueOf(s.getGradeLevel());
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);

        if (columnIndex == 0) {
            Integer id = Integer.valueOf(s.getStudentId());
            if (Boolean.TRUE.equals(aValue)) {
                deletedIds.add(id);
            } else {
                deletedIds.remove(id);
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            return;
        }

        if (columnIndex == 2) {
            String val = String.valueOf(aValue).trim();
            if (val.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null, "First name cannot be blank.");
            } else {
                s.setFirstName(val);
            }
        } else if (columnIndex == 3) {
            String val = String.valueOf(aValue).trim();
            if (val.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null, "Last name cannot be blank.");
            } else {
                s.setLastName(val);
            }
        } else if (columnIndex == 4) {
            try {
                int grade = Integer.parseInt(String.valueOf(aValue).trim());
                if (grade <= 0) {
                    JOptionPane.showMessageDialog(null, "Grade must be a positive number.");
                } else {
                    s.setGradeLevel(grade);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Grade must be a number.");
            }
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /** Get the student object for a row. */
    public Student getStudent(int row) {
        return students.get(row);
    }

    /** Current list used by the table. */
    public List<Student> getStudents() {
        return students;
    }

    /** Add a new row at the end. */
    public void addStudent(Student s) {
        students.add(s);
        int row = students.size() - 1;
        fireTableRowsInserted(row, row);
    }

    /** Remove one row. */
    public void removeStudent(int row) {
        students.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /** Toggle the delete checkbox for a row. */
    public void markDeleted(int row) {
        Student s = students.get(row);
        Integer id = Integer.valueOf(s.getStudentId());
        if (deletedIds.contains(id)) {
            deletedIds.remove(id);
        } else {
            deletedIds.add(id);
        }
        fireTableRowsUpdated(row, row);
    }

    /** True if that row is marked to delete. */
    public boolean isMarkedForDeletion(int row) {
        Student s = students.get(row);
        return deletedIds.contains(Integer.valueOf(s.getStudentId()));
    }

    /** Copy of all IDs marked to delete. */
    public Set<Integer> getDeletedIds() {
        return new HashSet<Integer>(deletedIds);
    }

    /** Clear delete marks and refresh. */
    public void clearDeleted() {
        deletedIds.clear();
        fireTableDataChanged();
    }

    /** Replace all rows and refresh. */
    public void setRows(List<Student> newRows) {
        students.clear();
        if (newRows != null) {
            students.addAll(newRows);
        }
        fireTableDataChanged();
    }

    /** Older name kept for callers that still use it. */
    public void setStudents(List<Student> list) {
        setRows(list);
    }
}
