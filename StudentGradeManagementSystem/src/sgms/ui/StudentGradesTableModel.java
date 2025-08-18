package sgms.ui;

import sgms.model.Assignment;
import sgms.model.Student;
import sgms.dao.GradeDAO;                 // kept for older code paths
import sgms.service.ValidationService;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows students and their RAW marks for each assignment.
 * Col 0 = Student name, Col 1-N = marks (editable).
 * Also exposes helpers to get term and final percentages.
 */
public class StudentGradesTableModel extends AbstractTableModel {

    private final List<Student> students;
    private final List<Assignment> assignments;
    // studentId -> (assignmentId -> RAW mark)
    private final Map<Integer, Map<Integer, Integer>> grades;

    public StudentGradesTableModel(List<Student> students,
                                   List<Assignment> assignments,
                                   Map<Integer, Map<Integer, Integer>> grades) {
        this.students = (students == null)
                ? new ArrayList<Student>()
                : new ArrayList<Student>(students);

        this.assignments = (assignments == null)
                ? new ArrayList<Assignment>()
                : new ArrayList<Assignment>(assignments);

        this.grades = new HashMap<Integer, Map<Integer, Integer>>();
        if (students != null && grades != null) {
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                Map<Integer, Integer> m = grades.get(s.getStudentId());
                if (m == null) m = new HashMap<Integer, Integer>();
                this.grades.put(s.getStudentId(), m);
            }
        }
    }

    /** Older constructor signature; DAO is not used here. */
    public StudentGradesTableModel(List<Student> students,
                                   List<Assignment> assignments,
                                   Map<Integer, Map<Integer, Integer>> grades,
                                   GradeDAO ignored) {
        this(students, assignments, grades);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        // "Student" + one column per assignment
        return 1 + assignments.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "Student";
        Assignment a = assignments.get(column - 1);
        int max = (a.getMaxMarks() == null) ? 0 : a.getMaxMarks().intValue();
        return a.getTitle() + " (" + max + ")";
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) {
            String first = (s.getFirstName() == null) ? "" : s.getFirstName();
            String last  = (s.getLastName()  == null) ? "" : s.getLastName();
            return (first.length() > 0 && last.length() > 0) ? (first + " " + last) : (first + last);
        }
        int assignmentId = assignments.get(columnIndex - 1).getAssignmentId();
        Map<Integer, Integer> m = grades.get(s.getStudentId());
        if (m == null) return null;
        return m.get(assignmentId); // raw mark or null
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // only assignment marks are editable
        return columnIndex > 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) return;

        Student s = students.get(rowIndex);
        Assignment a = assignments.get(columnIndex - 1);
        int assignmentId = a.getAssignmentId();

        // allow clearing a mark by leaving the cell blank
        if (aValue == null || String.valueOf(aValue).trim().length() == 0) {
            Map<Integer, Integer> m = grades.get(s.getStudentId());
            if (m != null) {
                m.remove(assignmentId);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
            return;
        }

        try {
            int mark = Integer.parseInt(String.valueOf(aValue).trim());
            Integer maxObj = a.getMaxMarks();
            int upper = (maxObj != null) ? maxObj.intValue() : 100;

            if (!ValidationService.isIntInRange(mark, 0, upper)) {
                JOptionPane.showMessageDialog(null, "Grade must be between 0 and " + upper + ".");
                return;
            }

            Map<Integer, Integer> m = grades.get(s.getStudentId());
            if (m == null) {
                m = new HashMap<Integer, Integer>();
                grades.put(s.getStudentId(), m);
            }
            m.put(assignmentId, Integer.valueOf(mark));
            fireTableCellUpdated(rowIndex, columnIndex);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Grade must be a whole number.");
        }
    }

    // ---- basic helpers ----

    public Assignment getAssignmentAt(int index) {
        return assignments.get(index);
    }

    public void addAssignment(Assignment a) {
        assignments.add(a);
        fireTableStructureChanged();
    }

    public void removeAssignment(int index) {
        Assignment a = assignments.remove(index);
        // remove stored marks for that assignment
        for (Map.Entry<Integer, Map<Integer, Integer>> e : grades.entrySet()) {
            Map<Integer, Integer> m = e.getValue();
            if (m != null) m.remove(a.getAssignmentId());
        }
        fireTableStructureChanged();
    }

    /** Used by the save action. */
    public Map<Integer, Map<Integer, Integer>> getGrades() {
        return grades;
    }

    /** Older name kept so existing calls still compile. */
    public Map<Integer, Map<Integer, Integer>> getGradesByStudent() {
        return grades;
    }

    // ---- percentage helpers for “Final Grades” view ----

    /**
     * Average of assignment percentages in a term for one student.
     * percent = (raw / maxMarks) * 100
     * Returns null if there are no marks in that term.
     */
    public Double getTermAveragePercent(int studentId, int term) {
        Map<Integer, Integer> m = grades.get(studentId);
        if (m == null) return null;

        double sumPct = 0.0;
        int count = 0;

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            if (a.getTerm() == term) {
                Integer raw = m.get(a.getAssignmentId());
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

    /**
     * Weighted final percent using the term values.
     * (Weights are handled in GradeCalculator.)
     */
    public Double getFinalPercent(int studentId) {
        Double t1 = getTermAveragePercent(studentId, 1);
        Double t2 = getTermAveragePercent(studentId, 2);
        Double t3 = getTermAveragePercent(studentId, 3);
        Double t4 = getTermAveragePercent(studentId, 4);
        return sgms.util.GradeCalculator.calculateFinalGrade(t1, t2, t3, t4);
    }
}
