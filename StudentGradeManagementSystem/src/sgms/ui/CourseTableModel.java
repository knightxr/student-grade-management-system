package sgms.ui;

import sgms.model.Course;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table model representing courses with student counts.
 */
public class CourseTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Code", "Name", "Students"};
    private final List<Course> courses;
    private final Set<Integer> deletedIds = new HashSet<>();

    public CourseTableModel(List<Course> list) {
        this.courses = new ArrayList<>(list);
    }

    @Override
    public int getRowCount() {
        return courses.size();
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
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 3 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2; // only course name editable
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Course c = courses.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.getCourseId();
            case 1 -> c.getCourseCode();
            case 2 -> c.getCourseName();
            case 3 -> c.getStudentCount();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 2) {
            Course c = courses.get(rowIndex);
            c.setCourseName(String.valueOf(aValue));
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    public Course getCourse(int row) {
        return courses.get(row);
    }

    public void addCourse(Course c) {
        courses.add(c);
        int row = courses.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void markDeleted(int row) {
        Course c = courses.get(row);
        int id = c.getCourseId();
        if (deletedIds.contains(id)) {
            deletedIds.remove(id);
        } else {
            deletedIds.add(id);
        }
        fireTableRowsUpdated(row, row);
    }

    public boolean isMarkedForDeletion(int row) {
        return deletedIds.contains(courses.get(row).getCourseId());
    }

    public Set<Integer> getDeletedIds() {
        return new HashSet<>(deletedIds);
    }

    public List<Course> getCourses() {
        return courses;
    }
}