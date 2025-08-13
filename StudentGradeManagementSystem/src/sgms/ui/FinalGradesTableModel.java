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
        return switch (columnIndex) {
            case 0, 1 -> String.class;
            default -> Integer.class;
        };
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        FinalGrade g = grades.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> g.getFirstName();
            case 1 -> g.getLastName();
            case 2 -> g.getTerm1();
            case 3 -> g.getTerm2();
            case 4 -> g.getTerm3();
            case 5 -> g.getTerm4();
            case 6 -> g.getFinalGrade();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}