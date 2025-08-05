package sgms.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import sgms.model.Student;

/**
 * Table model backing the JTable on the MainPage when viewing students.
 * It holds Student objects and exposes them as editable rows.
 */
public class StudentTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "First Name", "Last Name", "Grade"};
    private final List<Student> students;

    public StudentTableModel(List<Student> list) {
        this.students = new ArrayList<>(list);
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> s.getStudentId();
            case 1 -> s.getFirstName();
            case 2 -> s.getLastName();
            case 3 -> s.getGradeLevel();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != 0; // ID is read-only
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        switch (columnIndex) {
            case 1 -> s.setFirstName(String.valueOf(aValue));
            case 2 -> s.setLastName(String.valueOf(aValue));
            case 3 -> {
                try {
                    s.setGradeLevel(Integer.parseInt(String.valueOf(aValue)));
                } catch (NumberFormatException e) {
                    // Ignore invalid numbers; keep previous value
                }
            }
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

    public void setStudents(List<Student> list) {
        students.clear();
        students.addAll(list);
        fireTableDataChanged();
    }
}