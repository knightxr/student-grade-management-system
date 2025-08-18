package sgms.ui;

import sgms.model.Student;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for attendance over one school week (Mon–Fri).
 */
public class AttendanceTableModel extends AbstractTableModel {

    private final List<Student> students;
    private final LocalDate[] weekDays; // Monday..Friday
    // studentId -> (date -> present)
    private final Map<Integer, Map<LocalDate, Boolean>> attendance;
    // only the cells changed in the UI, for saving
    private final Map<Integer, Map<LocalDate, Boolean>> changes = new HashMap<Integer, Map<LocalDate, Boolean>>();

    public AttendanceTableModel(List<Student> students, LocalDate startOfWeek,
                                Map<Integer, Map<LocalDate, Boolean>> attendance) {
        this.students = new ArrayList<Student>(students);

        this.weekDays = new LocalDate[5];
        for (int i = 0; i < 5; i++) {
            this.weekDays[i] = startOfWeek.plusDays(i);
        }

        // make a copy per student; avoid getOrDefault/emptyMap for simplicity
        this.attendance = new HashMap<Integer, Map<LocalDate, Boolean>>();
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            Map<LocalDate, Boolean> src = attendance != null
                    ? attendance.get(Integer.valueOf(s.getStudentId()))
                    : null;
            Map<LocalDate, Boolean> copy = new HashMap<LocalDate, Boolean>();
            if (src != null) {
                copy.putAll(src);
            }
            this.attendance.put(Integer.valueOf(s.getStudentId()), copy);
        }
    }

    // Backward-compatible constructor (DAO is ignored)
    public AttendanceTableModel(List<Student> students, LocalDate startOfWeek,
                                Map<Integer, Map<LocalDate, Boolean>> attendance,
                                sgms.dao.AttendanceDAO ignored) {
        this(students, startOfWeek, attendance);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return 1 + weekDays.length;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) return "Student";
        LocalDate d = weekDays[column - 1];
        String day = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
        return day + " " + d.getDayOfMonth();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Boolean.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        if (columnIndex == 0) {
            return s.getLastName() + ", " + s.getFirstName();
        }
        LocalDate d = weekDays[columnIndex - 1];
        Map<LocalDate, Boolean> perStudent = attendance.get(Integer.valueOf(s.getStudentId()));
        if (perStudent == null) return Boolean.FALSE;
        Boolean val = perStudent.get(d);
        return val != null ? val : Boolean.FALSE;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Student s = students.get(rowIndex);
        LocalDate d = weekDays[columnIndex - 1];
        boolean present = Boolean.TRUE.equals(aValue);

        Integer key = Integer.valueOf(s.getStudentId());

        Map<LocalDate, Boolean> perStudent = attendance.get(key);
        if (perStudent == null) {
            perStudent = new HashMap<LocalDate, Boolean>();
            attendance.put(key, perStudent);
        }
        perStudent.put(d, Boolean.valueOf(present));

        Map<LocalDate, Boolean> changed = changes.get(key);
        if (changed == null) {
            changed = new HashMap<LocalDate, Boolean>();
            changes.put(key, changed);
        }
        changed.put(d, Boolean.valueOf(present));

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    /** Returns only the cells changed by the user. */
    public Map<Integer, Map<LocalDate, Boolean>> getChanges() {
        return changes;
    }

    /** Clears the change tracker after saving. */
    public void clearChanges() {
        changes.clear();
    }

    public LocalDate[] getWeekDays() {
        return weekDays;
    }
}
