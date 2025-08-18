package sgms.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Small helper that adds a text search to a JTable.
 * Works with the table's sorter and keeps things simple.
 */
public final class SearchUtil {

    private SearchUtil() {}

    /** Wire up the text field and button to filter the given table. */
    public static void installSearch(final JTable table,
                                     final JTextField field,
                                     final JButton button) {

        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter(table, field.getText());
            }
        };

        button.addActionListener(action);
        field.addActionListener(action);
    }

    /** Apply or clear the filter on the table. */
    private static void applyFilter(JTable table, String text) {
        // Ensure there is a TableRowSorter and that it points at the CURRENT model.
        TableRowSorter<TableModel> sorter;
        if (table.getRowSorter() instanceof TableRowSorter) {
            sorter = (TableRowSorter<TableModel>) table.getRowSorter();
            // Important when the model changes after search is installed
            sorter.setModel(table.getModel());
        } else {
            sorter = new TableRowSorter<TableModel>(table.getModel());
            table.setRowSorter(sorter);
        }

        // Empty text = show all rows
        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            String q = text.trim();
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        }

        table.clearSelection();
        table.revalidate();
        table.repaint();
    }
}
