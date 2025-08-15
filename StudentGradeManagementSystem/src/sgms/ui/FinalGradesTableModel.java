package sgms.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import sgms.model.Student;

/**
 * Final Grades summary for a selected grade (across ALL its courses).
 * Columns: First Name | Last Name | T1 | T2 | T3 | T4 | Final %
 *
 * This table DOES NOT edit data. It reads all raw marks from a backing
 * StudentGradesTableModel and converts them to:
 *   term average %  = average of (mark / maxMarks * 100) for assignments in that term
 *   final %         = GradeCalculator.calculateFinalGrade(T1,T2,T3,T4)  (weighted)
 *
 * IEB-friendly: simple loops; no streams/lambdas.
 */
public class FinalGradesTableModel extends AbstractTableModel {

    private final List<Student> students;                 // rows
    private final StudentGradesTableModel backing;        // provides assignments + raw marks

    private static final String[] COLS =
            { "First Name", "Last Name", "T1", "T2", "T3", "T4", "Final %" };

    public FinalGradesTableModel(List<Student> students, StudentGradesTableModel backing) {
        if (students == null) {
            this.students = new ArrayList<Student>();
        } else {
            this.students = new ArrayList<Student>(students);
        }
        this.backing = backing;
    }

    @Override
    public int getRowCount() { return students.size(); }

    @Override
    public int getColumnCount() { return COLS.length; }

    @Override
    public String getColumnName(int column) { return COLS[column]; }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: case 1: return String.class;    // names
            default:        return Integer.class;   // T1..T4, Final %
        }
    }

    @Override
    public boolean isCellEditable(int r, int c) { return false; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) return s.getFirstName();
        if (columnIndex == 1) return s.getLastName();

        // Term averages
        if (columnIndex >= 2 && columnIndex <= 5) {
            int term = (columnIndex - 2) + 1; // 2 maps to 1, 3 to 2, 4 to 3, 5 to 4
            Double t = backing.getTermAveragePercent(s.getStudentId(), term);
            return (t == null) ? null : Integer.valueOf((int)Math.round(t.doubleValue()));
        }

        // Final %
        Double fin = backing.getFinalPercent(s.getStudentId());
        return (fin == null) ? null : Integer.valueOf((int)Math.round(fin.doubleValue()));
    }

}