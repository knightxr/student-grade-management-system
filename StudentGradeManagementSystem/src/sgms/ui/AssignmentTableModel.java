package sgms.ui;

import sgms.model.Assignment;

import javax.swing.table.AbstractTableModel;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sgms.dao.AssignmentDAO;

/**
 * Table model representing assignments for a course.
 */
public class AssignmentTableModel extends AbstractTableModel {

    private final String[] columns = {"Delete", "ID", "Title", "Max Marks", "Term", "Due Date"};
    private final List<Assignment> assignments;
    private final Set<Integer> deletedIds = new HashSet<>();

    public AssignmentTableModel(List<Assignment> list) {
        this.assignments = new ArrayList<>(list);
    }

    /**
     * Backward-compatible constructor that accepts a DAO parameter which is no
     * longer used by the model. Delegates to the primary constructor.
     */
    public AssignmentTableModel(List<Assignment> list, AssignmentDAO assignmentDAO) {
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
            case 0:
                return Boolean.class;
            case 1:
            case 3:
            case 4:
                return Integer.class;
            case 5:
                return Date.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 || columnIndex >= 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Assignment a = assignments.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return deletedIds.contains(a.getAssignmentId());
            case 1:
                return a.getAssignmentId();
            case 2:
                return a.getTitle();
            case 3:
                return a.getMaxMarks();
            case 4:
                return a.getTerm();
            case 5:
                return a.getDueDate();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Assignment a = assignments.get(rowIndex);
        if (columnIndex == 0) {
            int id = a.getAssignmentId();
            if (Boolean.TRUE.equals(aValue)) {
                deletedIds.add(id);
            } else {
                deletedIds.remove(id);
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
            return;
        }
        if (columnIndex == 2) {
            a.setTitle(String.valueOf(aValue));
        } else if (columnIndex == 3) {
            try {
                if (aValue == null || String.valueOf(aValue).trim().isEmpty()) {
                    a.setMaxMarks(null);
                } else {
                    a.setMaxMarks(Integer.parseInt(String.valueOf(aValue).trim()));
                }
            } catch (NumberFormatException e) {
                // ignore invalid input
            }
        } else if (columnIndex == 4) {
            try {
                if (aValue != null && !String.valueOf(aValue).trim().isEmpty()) {
                    a.setTerm(Integer.parseInt(String.valueOf(aValue).trim()));
                }
            } catch (NumberFormatException e) {
                // ignore invalid input
            }
        } else if (columnIndex == 5) {
            if (aValue == null || String.valueOf(aValue).trim().isEmpty()) {
                a.setDueDate(null);
            } else if (aValue instanceof java.util.Date) {
                java.util.Date d = (java.util.Date) aValue;
                a.setDueDate(new Date(d.getTime()));
            } else {
                try {
                    a.setDueDate(Date.valueOf(String.valueOf(aValue)));
                } catch (IllegalArgumentException ex) {
                    // ignore invalid date
                }
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
        int id = a.getAssignmentId();
        if (deletedIds.contains(id)) {
            deletedIds.remove(id);
        } else {
            deletedIds.add(id);
        }
        fireTableRowsUpdated(row, row);
    }

    public boolean isMarkedForDeletion(int row) {
        if (row < 0 || row >= assignments.size()) {
            return false;
        }
        return deletedIds.contains(assignments.get(row).getAssignmentId());
    }

    public Set<Integer> getDeletedIds() {
        return new HashSet<>(deletedIds);
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}