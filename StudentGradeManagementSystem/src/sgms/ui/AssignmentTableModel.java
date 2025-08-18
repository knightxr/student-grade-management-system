package sgms.ui;

import sgms.model.Assignment;

import javax.swing.table.AbstractTableModel;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Table model for assignments in one course.
 * Columns: Delete checkbox, ID, Title, Max Marks, Term, Due Date.
 */
public class AssignmentTableModel extends AbstractTableModel {

    private final String[] columns = {"Delete", "ID", "Title", "Max Marks", "Term", "Due Date"};
    private final List<Assignment> assignments;
    private final Set<Integer> deletedIds = new HashSet<Integer>();

    public AssignmentTableModel(List<Assignment> list) {
        this.assignments = new ArrayList<Assignment>(list);
    }

    // Backwards-compatible constructor (DAO not used here)
    public AssignmentTableModel(List<Assignment> list, sgms.dao.AssignmentDAO ignored) {
        this(list);
    }

    @Override
    public int getRowCount() {
        return assignments.size();
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
            case 0: return Boolean.class; // delete checkbox
            case 1: return Integer.class; // ID
            case 3: return Integer.class; // Max Marks
            case 4: return Integer.class; // Term
            case 5: return Date.class;    // Due Date
            default: return String.class; // Title
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // Allow toggling delete and editing of Title/Max/Term/Due Date (ID is read-only)
        return columnIndex == 0 || columnIndex >= 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Assignment a = assignments.get(rowIndex);
        switch (columnIndex) {
            case 0: return deletedIds.contains(a.getAssignmentId());
            case 1: return Integer.valueOf(a.getAssignmentId());
            case 2: return a.getTitle();
            case 3: return a.getMaxMarks();
            case 4: return Integer.valueOf(a.getTerm());
            case 5: return a.getDueDate();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Assignment a = assignments.get(rowIndex);

        if (columnIndex == 0) {
            // toggle delete flag for this assignment ID
            int id = a.getAssignmentId();
            if (Boolean.TRUE.equals(aValue)) {
                deletedIds.add(Integer.valueOf(id));
            } else {
                deletedIds.remove(Integer.valueOf(id));
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            return;
        }

        if (columnIndex == 2) { // Title
            a.setTitle(String.valueOf(aValue));
        } else if (columnIndex == 3) { // Max Marks
            try {
                if (aValue == null || String.valueOf(aValue).trim().isEmpty()) {
                    a.setMaxMarks(null);
                } else {
                    int v = Integer.parseInt(String.valueOf(aValue).trim());
                    if (v >= 0) { // keep it simple: non-negative only
                        a.setMaxMarks(Integer.valueOf(v));
                    }
                }
            } catch (NumberFormatException ignore) { /* leave as is */ }
        } else if (columnIndex == 4) { // Term
            try {
                if (aValue != null && !String.valueOf(aValue).trim().isEmpty()) {
                    int t = Integer.parseInt(String.valueOf(aValue).trim());
                    if (t >= 1 && t <= 4) {
                        a.setTerm(t);
                    }
                }
            } catch (NumberFormatException ignore) { /* leave as is */ }
        } else if (columnIndex == 5) { // Due Date
            if (aValue == null || String.valueOf(aValue).trim().isEmpty()) {
                a.setDueDate(null);
            } else if (aValue instanceof java.util.Date) {
                java.util.Date d = (java.util.Date) aValue;
                a.setDueDate(new Date(d.getTime()));
            } else {
                try {
                    a.setDueDate(Date.valueOf(String.valueOf(aValue)));
                } catch (IllegalArgumentException ignore) { /* leave as is */ }
            }
        }

        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public Assignment getAssignment(int row) {
        return assignments.get(row);
    }

    public void addAssignment(Assignment a) {
        assignments.add(a);
        int row = assignments.size() - 1;
        fireTableRowsInserted(row, row);
    }

    public void markDeleted(int row) {
        Assignment a = assignments.get(row);
        Integer id = Integer.valueOf(a.getAssignmentId());
        if (deletedIds.contains(id)) {
            deletedIds.remove(id);
        } else {
            deletedIds.add(id);
        }
        fireTableRowsUpdated(row, row);
    }

    public boolean isMarkedForDeletion(int row) {
        if (row < 0 || row >= assignments.size()) return false;
        return deletedIds.contains(Integer.valueOf(assignments.get(row).getAssignmentId()));
    }

    public Set<Integer> getDeletedIds() {
        return new HashSet<Integer>(deletedIds);
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}
