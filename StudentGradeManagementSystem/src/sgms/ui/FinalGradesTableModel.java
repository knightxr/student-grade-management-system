package sgms.ui;

import sgms.model.FinalGrade;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import sgms.dao.FinalGradeDAO;

/**
 * Table model for final grades display (read-only).
 */
public class FinalGradesTableModel extends AbstractTableModel {

    private final String[] columns = {"Name", "Surname", "Term 1", "Term 2", "Term 3", "Term 4", "Final"};
    private final List<FinalGrade> grades;

    public FinalGradesTableModel(List<FinalGrade> list) {
        this.grades = new ArrayList<>(list);
    }

    /**
     * Backward-compatible constructor that accepts a DAO parameter which is no
     * longer used.
     */
    public FinalGradesTableModel(List<FinalGrade> list, FinalGradeDAO dao) {
        this(list);
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
        switch (columnIndex) {
            case 0:
                return g.getFirstName();
            case 1:
                return g.getLastName();
            case 2:
                return format(g.getTerm1());
            case 3:
                return format(g.getTerm2());
            case 4:
                return format(g.getTerm3());
            case 5:
                return format(g.getTerm4());
            case 6:
                return format(g.getFinalGrade());
            default:
                return null;
        }
    }

    private String format(Integer value) {
        return value != null ? value + "%" : "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}