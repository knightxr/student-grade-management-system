package sgms.ui;

import sgms.model.Assignment;
import sgms.model.Student;
import sgms.dao.GradeDAO;                 // back-compat ctor only
import sgms.service.ValidationService;  // used for simple range checks

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table model showing students and their grades for each assignment.
 * - Column 0: "Student" (First Last)
 * - Columns 1..N: RAW marks per assignment (editable)
 *
 * Public helpers (used by FinalGradesTableModel):
 *   - getTermAveragePercent(studentId, term)
 *   - getFinalPercent(studentId)   // uses GradeCalculator (weighted)
 *
 * IEB-aligned: simple loops, no streams/lambdas.
 */
public class StudentGradesTableModel extends AbstractTableModel {

    private final List<Student> students;
    private final List<Assignment> assignments;
    // studentId maps to (assignmentId maps to RAW mark)
    private final Map<Integer, Map<Integer, Integer>> grades;

    public StudentGradesTableModel(List<Student> students,
                                   List<Assignment> assignments,
                                   Map<Integer, Map<Integer, Integer>> grades) {
        this.students = (students == null) ? new ArrayList<Student>()
                                           : new ArrayList<Student>(students);
        this.assignments = (assignments == null) ? new ArrayList<Assignment>()
                                                 : new ArrayList<Assignment>(assignments);
        this.grades = new HashMap<Integer, Map<Integer, Integer>>();
        if (students != null && grades != null) {
            int i;
            for (i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                Map<Integer, Integer> m = grades.get(s.getStudentId());
                if (m == null) m = new HashMap<Integer, Integer>();
                this.grades.put(s.getStudentId(), m);
            }
        }
    }

    /** Backward-compatible constructor; DAO is ignored by the model. */
    public StudentGradesTableModel(List<Student> students,
                                   List<Assignment> assignments,
                                   Map<Integer, Map<Integer, Integer>> grades,
                                   GradeDAO gradeDAO) {
        this(students, assignments, grades);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        // "Student" + all assignment columns
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
            String first = s.getFirstName() == null ? "" : s.getFirstName();
            String last  = s.getLastName()  == null ? "" : s.getLastName();
            String space = (first.length() > 0 && last.length() > 0) ? " " : "";
            return first + space + last;
        }
        int assignmentId = assignments.get(columnIndex - 1).getAssignmentId();
        Map<Integer, Integer> studentGrades = grades.get(s.getStudentId());
        if (studentGrades == null) return null;
        return studentGrades.get(assignmentId); // RAW mark (may be null)
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0; // assignments editable; name not editable
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) return;

        Student s = students.get(rowIndex);
        Assignment a = assignments.get(columnIndex - 1);
        int assignmentId = a.getAssignmentId();

        // allow clearing a mark by leaving blank
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
            JOptionPane.showMessageDialog(null, "Grade must be a number between 0 and 100.");
        }
    }

    // ------------ Existing utilities ------------

    public Assignment getAssignmentAt(int index) {
        return assignments.get(index);
    }

    public void addAssignment(Assignment a) {
        assignments.add(a);
        fireTableStructureChanged();
    }

    public void removeAssignment(int index) {
        Assignment a = assignments.remove(index);
        // Remove any stored marks for this assignment
        java.util.Iterator<Map.Entry<Integer, Map<Integer, Integer>>> it = grades.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Map<Integer, Integer>> e = it.next();
            Map<Integer, Integer> m = e.getValue();
            if (m != null) m.remove(a.getAssignmentId());
        }
        fireTableStructureChanged();
    }

    /** Original getter used by save handler. */
    public Map<Integer, Map<Integer, Integer>> getGrades() {
        return grades;
    }

    /** Back-compat alias (so existing calls to getGradesByStudent() compile). */
    public Map<Integer, Map<Integer, Integer>> getGradesByStudent() {
        return grades;
    }

    // ------------ Helpers for Final grades view ------------

    /**
     * Returns the average of assignment PERCENTs (0..100) for the given student and term,
     * or null if the student has no marks in that term.
     * percent = (raw / maxMarks) * 100
     */
    public Double getTermAveragePercent(int studentId, int term) {
        Map<Integer, Integer> m = grades.get(studentId);
        if (m == null) return null;

        double sumPct = 0.0;
        int count = 0;

        int i;
        for (i = 0; i < assignments.size(); i++) {
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
     * Returns the weighted FINAL percent using GradeCalculator with term percents.
     * Missing terms are ignored and weights are re-normalised in GradeCalculator.
     */
    public Double getFinalPercent(int studentId) {
        Double t1 = getTermAveragePercent(studentId, 1);
        Double t2 = getTermAveragePercent(studentId, 2);
        Double t3 = getTermAveragePercent(studentId, 3);
        Double t4 = getTermAveragePercent(studentId, 4);
        return sgms.util.GradeCalculator.calculateFinalGrade(t1, t2, t3, t4);
    }
}
