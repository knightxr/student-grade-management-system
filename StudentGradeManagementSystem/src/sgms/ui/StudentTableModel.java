package sgms.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import sgms.model.Student;
import sgms.dao.StudentDAO;

/**
 * Table model backing the JTable on the MainPage when viewing students.
 * It holds Student objects and exposes them as editable rows.
 * IEB-friendly: no streams or lambdas; clear, simple loops.
 */
public class StudentTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "First Name", "Last Name", "Grade"};
    private final List<Student> students;

    public StudentTableModel(List<Student> list) {
        // Defensive copy
        if (list == null) {
            this.students = new ArrayList<Student>();
        } else {
            this.students = new ArrayList<Student>(list);
        }
    }

    /** Backward-compatible constructor (DAO is not used by the model). */
    public StudentTableModel(List<Student> list, StudentDAO studentDAO) {
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

    /** Make Grade and ID numeric so sorting and grouping work correctly. */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class; // ID
            case 1: return String.class;  // First Name
            case 2: return String.class;  // Last Name
            case 3: return Integer.class; // Grade
            default: return Object.class;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        switch (columnIndex) {
            case 0: return s.getStudentId();
            case 1: return s.getFirstName();
            case 2: return s.getLastName();
            case 3: return s.getGradeLevel();
            default: return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0; // ID is read-only
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        switch (columnIndex) {
            case 1: {
                String val = String.valueOf(aValue).trim();
                if (val.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "First name cannot be blank.");
                } else {
                    s.setFirstName(val);
                }
                break;
            }
            case 2: {
                String val = String.valueOf(aValue).trim();
                if (val.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Last name cannot be blank.");
                } else {
                    s.setLastName(val);
                }
                break;
            }
            case 3: {
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
                break;
            }
            default:
                break;
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public Student getStudent(int row) {
        return students.get(row);
    }

    public List<Student> getStudents() {
        return students;
    }

    public void addStudent(Student s) {
        students.add(s);
        int row = students.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void removeStudent(int row) {
        students.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /** Replace all rows and notify listeners. */
    public void setRows(List<Student> newRows) {
        students.clear();
        if (newRows != null) {
            students.addAll(newRows);
        }
        fireTableDataChanged();
    }

    public void setStudents(List<Student> list) {
        setRows(list);
    }
}
