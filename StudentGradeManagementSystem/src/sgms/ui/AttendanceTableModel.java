package sgms.ui;

import sgms.model.Student;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.util.Locale;
import sgms.dao.AttendanceDAO;

/**
 * Table model for displaying and editing student attendance for a week.
 */
public class AttendanceTableModel extends AbstractTableModel {

    private final List<Student> students;
    private final LocalDate[] weekDays; // Monday..Friday
    private final Map<Integer, Map<LocalDate, Boolean>> attendance;
    private final Map<Integer, Map<LocalDate, Boolean>> changes = new HashMap<>();

    public AttendanceTableModel(List<Student> students, LocalDate startOfWeek,
                                Map<Integer, Map<LocalDate, Boolean>> attendance) {
        this.students = new ArrayList<>(students);
        this.weekDays = new LocalDate[5];
        for (int i = 0; i < 5; i++) {
            this.weekDays[i] = startOfWeek.plusDays(i);
        }
        this.attendance = new HashMap<>();
        for (Student s : students) {
            Map<LocalDate, Boolean> map = attendance.getOrDefault(s.getStudentId(), Collections.emptyMap());
            this.attendance.put(s.getStudentId(), new HashMap<>(map));
        }
    }

    /**
     * Backward-compatible constructor accepting a DAO parameter that is
     * ignored. Delegates to the primary constructor.
     */
    public AttendanceTableModel(List<Student> students, LocalDate startOfWeek,
                                Map<Integer, Map<LocalDate, Boolean>> attendance,
                                AttendanceDAO attendanceDAO) {
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
        if (column == 0) {
            return "Student";
        }
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
        return attendance.getOrDefault(s.getStudentId(), Collections.emptyMap())
                         .getOrDefault(d, Boolean.FALSE);
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
        attendance.computeIfAbsent(s.getStudentId(), k -> new HashMap<>()).put(d, present);
        changes.computeIfAbsent(s.getStudentId(), k -> new HashMap<>()).put(d, present);
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public Map<Integer, Map<LocalDate, Boolean>> getChanges() {
        return changes;
    }

    public void clearChanges() {
        changes.clear();
    }

    public LocalDate[] getWeekDays() {
        return weekDays;
    }
}