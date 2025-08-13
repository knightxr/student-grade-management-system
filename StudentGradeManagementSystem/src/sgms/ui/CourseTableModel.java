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

    private final String[] columns = {"Delete", "ID", "Code", "Name", "Students"};
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
            case 0 -> Boolean.class;
            case 1, 4 -> Integer.class;
            default -> String.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex == 3; // delete checkbox and course name editable
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Course c = courses.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> deletedIds.contains(c.getCourseId());
            case 1 -> c.getCourseId();
            case 2 -> c.getCourseCode();
            case 3 -> c.getCourseName();
            case 4 -> c.getStudentCount();
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            Course c = courses.get(rowIndex);
            int id = c.getCourseId();
            if (Boolean.TRUE.equals(aValue)) {
                deletedIds.add(id);
            } else {
                deletedIds.remove(id);
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        } else if (columnIndex == 3) {
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