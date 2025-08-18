package sgms.ui;

import sgms.model.Student;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Final grades view for a selected grade across all courses.
 * Columns: First Name | Last Name | T1 | T2 | T3 | T4 | Final %
 * Reads data from StudentGradesTableModel and shows percentages.
 */
public class FinalGradesTableModel extends AbstractTableModel {

    private static final String[] COLS = {
            "First Name", "Last Name", "T1", "T2", "T3", "T4", "Final %"
    };

    private final List<Student> students;          // rows
    private final StudentGradesTableModel backing; // provides term/final % helpers

    public FinalGradesTableModel(List<Student> students, StudentGradesTableModel backing) {
        this.students = (students == null) ? new ArrayList<Student>() : new ArrayList<Student>(students);
        this.backing = backing;
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // Names = String, numbers = Integer (percentages, or null if missing)
        if (columnIndex == 0 || columnIndex == 1) return String.class;
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) return s.getFirstName();
        if (columnIndex == 1) return s.getLastName();

        // T1..T4
        if (columnIndex >= 2 && columnIndex <= 5) {
            int term = (columnIndex - 2) + 1; // 2->1, 3->2, 4->3, 5->4
            Double t = (backing == null) ? null : backing.getTermAveragePercent(s.getStudentId(), term);
            return (t == null) ? null : Integer.valueOf((int) Math.round(t.doubleValue()));
        }

        // Final %
        Double fin = (backing == null) ? null : backing.getFinalPercent(s.getStudentId());
        return (fin == null) ? null : Integer.valueOf((int) Math.round(fin.doubleValue()));
    }
}
