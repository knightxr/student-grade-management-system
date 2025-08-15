package sgms.util;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SearchUtilTest {

    private JTable table;
    private JTextField field;
    private JButton button;

    @Before
    public void setUp() {
        DefaultTableModel model = new DefaultTableModel(new Object[][]{
            {"Alice"}, {"Bob"}, {"Charlie"}
        }, new Object[]{"Name"});
        table = new JTable(model);
        field = new JTextField();
        button = new JButton();
        SearchUtil.installSearch(table, field, button);
    }

    private void triggerSearch() {
        ActionListener[] ls = button.getActionListeners();
        for (int i = 0; i < ls.length; i++) {
            ls[i].actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    @Test
    public void emptyQueryShowsAllRows() {
        field.setText("");
        triggerSearch();
        assertEquals(3, table.getRowCount());
    }

    @Test
    public void caseInsensitiveMatchFiltersRows() {
        field.setText("bob");
        triggerSearch();
        assertEquals(1, table.getRowCount());
    }

    @Test
    public void noMatchResultsInZeroRows() {
        field.setText("zzz");
        triggerSearch();
        assertEquals(0, table.getRowCount());
    }
}