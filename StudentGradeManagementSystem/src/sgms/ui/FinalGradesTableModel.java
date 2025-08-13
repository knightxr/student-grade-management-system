package sgms.ui;

import sgms.model.FinalGrade;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for final grades display (read-only).
 */
public class FinalGradesTableModel extends AbstractTableModel {

    private final String[] columns = {"Name", "Surname", "Term 1", "Term 2", "Term 3", "Term 4", "Final"};
    private final List<FinalGrade> grades;

    public FinalGradesTableModel(List<FinalGrade> list) {
        this.grades = new ArrayList<>(list);
    }

    @Override
    public int getRowCount() {
        return grades.size();
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
        return String.class;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FinalGrade g = grades.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> g.getFirstName();
            case 1 -> g.getLastName();
            case 2 -> format(g.getTerm1());
            case 3 -> format(g.getTerm2());
            case 4 -> format(g.getTerm3());
            case 5 -> format(g.getTerm4());
            case 6 -> format(g.getFinalGrade());
            default -> null;
        };
    }

    private String format(Integer value) {
        return value != null ? value + "%" : "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}