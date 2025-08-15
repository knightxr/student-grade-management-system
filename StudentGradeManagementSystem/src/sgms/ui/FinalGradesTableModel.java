package sgms.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

import sgms.model.Assignment;
import sgms.model.Student;
import sgms.util.GradeCalculator;

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
            int term = (columnIndex - 2) + 1; // 2->1, 3->2, 4->3, 5->4
            Double t = computeTermAveragePercent(s.getStudentId(), term);
            return (t == null) ? null : Integer.valueOf((int)Math.round(t.doubleValue()));
        }

        // Final %
        Double t1 = computeTermAveragePercent(s.getStudentId(), 1);
        Double t2 = computeTermAveragePercent(s.getStudentId(), 2);
        Double t3 = computeTermAveragePercent(s.getStudentId(), 3);
        Double t4 = computeTermAveragePercent(s.getStudentId(), 4);
        Double fin = GradeCalculator.calculateFinalGrade(t1, t2, t3, t4);
        return (fin == null) ? null : Integer.valueOf((int)Math.round(fin.doubleValue()));
    }

    // ---------------- helpers ----------------

    /** Average of assignment PERCENTs for the given term from the backing model. */
    private Double computeTermAveragePercent(int studentId, int term) {
        Map<Integer, Map<Integer, Integer>> grades = backing.getGrades();
        Map<Integer, Integer> marks = grades.get(studentId);
        if (marks == null) return null;

        double sumPct = 0.0;
        int count = 0;

        // backing has: column 0 = "Student", then assignments at indices 1..N
        int assignmentCount = backing.getColumnCount() - 1;
        int i;
        for (i = 0; i < assignmentCount; i++) {
            Assignment a = backing.getAssignmentAt(i);
            if (a.getTerm() == term) {
                Integer raw = marks.get(a.getAssignmentId());
                if (raw != null) {
                    int max = (a.getMaxMarks() == null) ? 0 : a.getMaxMarks().intValue();
                    double pct = (max > 0) ? (raw.intValue() * 100.0) / max : 0.0;
                    sumPct += pct;
                    count++;
                }
            }
        }

        if (count == 0) return null;
        return Double.valueOf(sumPct / count);
    }
}
