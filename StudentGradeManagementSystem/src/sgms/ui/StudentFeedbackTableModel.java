package sgms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import sgms.model.Student;
import sgms.dao.FeedbackDAO;

/** Table model for displaying student feedback notes. */
public class StudentFeedbackTableModel extends AbstractTableModel {

    private final String[] columns = {"First Name", "Last Name", "Comment"};
    private final List<Student> students;
    private final Map<Integer, String> comments; // studentId -> note
    private final int PREVIEW_LENGTH = 55;

    public StudentFeedbackTableModel(List<Student> students, Map<Integer, String> comments) {
        this.students = new ArrayList<>(students);
        this.comments = new HashMap<>(comments);
    }

    /**
     * Backward-compatible constructor that accepts a DAO parameter which is not
     * used by the model. Delegates to the primary constructor.
     */
    public StudentFeedbackTableModel(List<Student> students, Map<Integer, String> comments,
                                     FeedbackDAO feedbackDAO) {
        this(students, comments);
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
        switch (columnIndex) {
            case 0:
                return s.getFirstName();
            case 1:
                return s.getLastName();
            case 2:
                String note = comments.get(s.getStudentId());
                if (note == null) {
                    note = "";
                }
                if (note.length() > PREVIEW_LENGTH) {
                    note = note.substring(0, PREVIEW_LENGTH) + "...";
                }
                return note;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            Student s = students.get(rowIndex);
            comments.put(s.getStudentId(), String.valueOf(aValue));
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public Student getStudent(int row) {
        return students.get(row);
    }

    public String getFullComment(int row) {
        Student s = students.get(row);
        return comments.getOrDefault(s.getStudentId(), "");
    }

    public Map<Integer, String> getComments() {
        return comments;
    }

    public void setData(List<Student> students, Map<Integer, String> comments) {
        this.students.clear();
        this.students.addAll(students);
        this.comments.clear();
        this.comments.putAll(comments);
        fireTableDataChanged();
    }
}