package sgms.ui;

import sgms.model.Course;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table model for courses with a delete checkbox and student count.
 */
public class CourseTableModel extends AbstractTableModel {

    private final String[] columns = { "Delete", "ID", "Code", "Name", "Students" };
    private final List<Course> courses;
    private final Set<Integer> deletedIds = new HashSet<Integer>();

    public CourseTableModel(List<Course> list) {
        this.courses = new ArrayList<Course>(list);
    }

    // Backwards-compatible constructor (DAO not used)
    public CourseTableModel(List<Course> list, sgms.dao.CourseDAO ignored) {
        this(list);
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
        switch (columnIndex) {
            case 0: return Boolean.class; // Delete
            case 1: return Integer.class; // ID
            case 4: return Integer.class; // Students
            default: return String.class; // Code / Name
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Allow toggling delete and editing the course name (ID, Code, Students are read-only)
        return columnIndex == 0 || columnIndex == 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Course c = courses.get(rowIndex);
        switch (columnIndex) {
            case 0: return deletedIds.contains(Integer.valueOf(c.getCourseId()));
            case 1: return Integer.valueOf(c.getCourseId());
            case 2: return c.getCourseCode();
            case 3: return c.getCourseName();
            case 4: return Integer.valueOf(c.getStudentCount());
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Course c = courses.get(rowIndex);

        if (columnIndex == 0) {
            Integer id = Integer.valueOf(c.getCourseId());
            if (Boolean.TRUE.equals(aValue)) {
                deletedIds.add(id);
            } else {
                deletedIds.remove(id);
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            return;
        }

        if (columnIndex == 3) { // Name
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
        Integer id = Integer.valueOf(c.getCourseId());
        if (deletedIds.contains(id)) {
            deletedIds.remove(id);
        } else {
            deletedIds.add(id);
        }
        fireTableRowsUpdated(row, row);
    }

    public boolean isMarkedForDeletion(int row) {
        if (row < 0 || row >= courses.size()) return false;
        return deletedIds.contains(Integer.valueOf(courses.get(row).getCourseId()));
    }

    public Set<Integer> getDeletedIds() {
        return new HashSet<Integer>(deletedIds);
    }

    public List<Course> getCourses() {
        return courses;
    }
}
