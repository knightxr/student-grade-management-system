package sgms.ui;

import sgms.model.Assignment;
import sgms.model.Student;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import sgms.dao.GradeDAO;
import sgms.service.ValidationService;
import javax.swing.table.AbstractTableModel;

/**
 * Table model showing students and their grades for each assignment.
 */
public class StudentGradesTableModel extends AbstractTableModel {

    private final List<Student> students;
    private final List<Assignment> assignments;
    private final Map<Integer, Map<Integer, Integer>> grades; // studentId -> (assignmentId -> mark)

    public StudentGradesTableModel(List<Student> students, List<Assignment> assignments,
                                   Map<Integer, Map<Integer, Integer>> grades) {
        this.students = new ArrayList<>(students);
        this.assignments = new ArrayList<>(assignments);
        this.grades = new HashMap<>();
        for (Student s : students) {
            this.grades.put(s.getStudentId(), grades.getOrDefault(s.getStudentId(), new HashMap<>()));
        }
    }

    /**
     * Backward-compatible constructor that ignores the DAO parameter and
     * delegates to the primary constructor. The model no longer accesses the
     * database directly.
     */
    public StudentGradesTableModel(List<Student> students, List<Assignment> assignments,
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
        return 1 + assignments.size();
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Student";
        }
        return assignments.get(column - 1).getTitle();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) {
            return s.getFirstName() + " " + s.getLastName();
        }
        int assignmentId = assignments.get(columnIndex - 1).getAssignmentId();
        Map<Integer, Integer> studentGrades = grades.getOrDefault(s.getStudentId(), new HashMap<>());
        return studentGrades.get(assignmentId);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        int assignmentId = assignments.get(columnIndex - 1).getAssignmentId();
        try {
            int mark = Integer.parseInt(String.valueOf(aValue).trim());
            Assignment a = assignments.get(columnIndex - 1);
            Integer max = a.getMaxMarks();
            int upper = max != null ? max : 100;
            if (!ValidationService.isIntInRange(mark, 0, upper)) {
                JOptionPane.showMessageDialog(null, "Grade must be between 0 and " + upper + ".");
                return;
            }
            grades.computeIfAbsent(s.getStudentId(), k -> new HashMap<>()).put(assignmentId, mark);
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Grade must be a number between 0 and 100.");
        }
    }

    public Assignment getAssignmentAt(int index) {
        return assignments.get(index);
    }

    public void addAssignment(Assignment a) {
        assignments.add(a);
        fireTableStructureChanged();
    }

    public void removeAssignment(int index) {
        Assignment a = assignments.remove(index);
        for (Map<Integer, Integer> map : grades.values()) {
            // Remove grades for this assignment
            map.remove(a.getAssignmentId());
        }
        fireTableStructureChanged();
    }

    public Map<Integer, Map<Integer, Integer>> getGrades() {
        return grades;
    }
}