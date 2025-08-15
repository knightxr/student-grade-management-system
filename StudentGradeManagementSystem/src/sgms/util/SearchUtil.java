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

/** Simple text search for a JTable using its existing sorter. */
public final class SearchUtil {

    private SearchUtil() { }

    /** Call this after the table model is set. */
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

    private static void applyFilter(JTable table, String text) {
        // Use existing sorter if present; otherwise create one once
        if (!(table.getRowSorter() instanceof TableRowSorter)) {
            TableRowSorter<TableModel> sorter =
                    new TableRowSorter<TableModel>(table.getModel());
            table.setRowSorter(sorter);
        }

        TableRowSorter<?> sorter = (TableRowSorter<?>) table.getRowSorter();

        if (text == null || text.trim().isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            String q = text.trim();
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
        }

        // Make sure the table refreshes its view
        table.clearSelection();
        table.revalidate();
        table.repaint();
    }
}