package sgms.util;

import java.util.Arrays;
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
     * @param table the table to filter
     * @param searchField the text field containing the search query
     * @param searchButton the button used to trigger the search
     */
    public static void installSearch(JTable table, JTextField searchField, JButton searchButton) {
        Runnable action = () -> applyFilter(table, searchField.getText());
        searchButton.addActionListener(e -> action.run());
        searchField.addActionListener(e -> action.run());
    }

    /**
     * Applies a filter to the table based on the provided text. When the text
     * is empty or matches the placeholder value "Search", any existing filter
     * is cleared.
     *
     * @param table the table whose rows should be filtered
     * @param text the search text
     */
    public static void applyFilter(JTable table, String text) {
        RowSorter<? extends TableModel> sorter = table.getRowSorter();
        if (!(sorter instanceof TableRowSorter)) {
            // Ensure the table actually has a TableRowSorter to filter
            TableRowSorter<TableModel> trs = new TableRowSorter<>(table.getModel());
            table.setRowSorter(trs);
            sorter = trs;
        }

        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> rowSorter = (TableRowSorter<TableModel>) sorter;

        if (text == null || text.trim().isEmpty() || "Search".equals(text)) {
            rowSorter.setRowFilter(null);
            return;
        }

        final String[] parts = Arrays.stream(text.split("\\s+"))
                .map(String::toLowerCase)
                .toArray(String[]::new);

        rowSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < entry.getValueCount(); i++) {
                    Object v = entry.getValue(i);
                    if (v != null) {
                        sb.append(v).append(' ');
                    }
                }
                String rowText = sb.toString().toLowerCase().replace(",", "");
                for (String p : parts) {
                    if (!rowText.contains(p)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }
}
