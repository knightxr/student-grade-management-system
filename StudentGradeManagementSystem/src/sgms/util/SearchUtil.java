package sgms.util;

import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * Utility methods to enable simple text based searching on tables.
 */
public final class SearchUtil {

    private SearchUtil() {
    }

    /**
     * Installs listeners on the provided text field and button to filter the
     * contents of the table when triggered. Filtering is case-insensitive and
     * matches any column.
     *
     * @param table       the table to filter
     * @param searchField the text field containing the search query
     * @param searchButton the button used to trigger the search
     */
    public static void installSearch(JTable table, JTextField searchField, JButton searchButton) {
        Runnable action = () -> applyFilter(table, searchField.getText());
        searchButton.addActionListener(e -> action.run());
        searchField.addActionListener(e -> action.run());
    }

    /**
     * Applies a filter to the table based on the provided text. When the text is
     * empty or matches the placeholder value "Search", any existing filter is
     * cleared.
     *
     * @param table the table whose rows should be filtered
     * @param text  the search text
     */
    public static void applyFilter(JTable table, String text) {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (!(sorter instanceof TableRowSorter)) {
            return;
        }
        TableRowSorter<?> rowSorter = (TableRowSorter<?>) sorter;
        if (text == null || text.trim().isEmpty() || "Search".equals(text)) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text.trim())));
        }
    }
}