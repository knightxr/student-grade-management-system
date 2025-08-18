package sgms.ui;

import sgms.model.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows each student with a short preview of their comment.
 * Only the Comment column is editable.
 */
public class StudentFeedbackTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = { "First Name", "Last Name", "Comment" };
    private static final int PREVIEW_LENGTH = 55;

    private final List<Student> students;
    // studentId -> full comment text
    private final Map<Integer, String> comments;

    public StudentFeedbackTableModel(List<Student> students, Map<Integer, String> comments) {
        this.students = new ArrayList<Student>(students);
        this.comments = new HashMap<Integer, String>(comments);
    }

    // Backwards-compatible constructor (DAO not used)
    public StudentFeedbackTableModel(List<Student> students, Map<Integer, String> comments,
                                     sgms.dao.FeedbackDAO ignored) {
        this(students, comments);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) return s.getFirstName();
        if (columnIndex == 1) return s.getLastName();
        // Comment preview
        String note = comments.get(Integer.valueOf(s.getStudentId()));
        if (note == null) note = "";
        if (note.length() > PREVIEW_LENGTH) {
            note = note.substring(0, PREVIEW_LENGTH) + "...";
        }
        return note;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2; // only Comment
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 2) return;
        Student s = students.get(rowIndex);
        comments.put(Integer.valueOf(s.getStudentId()), String.valueOf(aValue));
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /** Get the student at a row. */
    public Student getStudent(int row) {
        return students.get(row);
    }

    /** Get the full (untrimmed) comment text for a row. */
    public String getFullComment(int row) {
        Student s = students.get(row);
        String note = comments.get(Integer.valueOf(s.getStudentId()));
        return note == null ? "" : note;
    }

    /** Map of studentId -> full comment text. */
    public Map<Integer, String> getComments() {
        return comments;
    }

    /** Replace all rows and comments at once. */
    public void setData(List<Student> newStudents, Map<Integer, String> newComments) {
        this.students.clear();
        this.students.addAll(newStudents);
        this.comments.clear();
        this.comments.putAll(newComments);
        fireTableDataChanged();
    }
}
