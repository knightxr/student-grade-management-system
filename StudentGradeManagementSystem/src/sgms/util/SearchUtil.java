// src/sgms/util/SearchUtil.java
package sgms.util;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.util.*;

public final class SearchUtil {
    private SearchUtil() {}

    /** Apply a smart, name-aware filter to a JTable. */
    public static void applySmartSearch(JTable table, String raw) {
        @SuppressWarnings("unchecked")
        TableRowSorter<TableModel> sorter =
                (table.getRowSorter() instanceof TableRowSorter)
                        ? (TableRowSorter<TableModel>) table.getRowSorter()
                        : new TableRowSorter<>((TableModel) table.getModel());

        if (table.getRowSorter() == null) {
            table.setRowSorter(sorter);
        }
        if (raw == null) { sorter.setRowFilter(null); return; }

        String q = raw.trim();
        if (q.isEmpty() || "Search".equalsIgnoreCase(q)) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(buildSmartNameRowFilter(table, q));
    }

    /** Build a filter that understands "First Last", "Last First", "Last, First". */
    public static RowFilter<TableModel, Integer> buildSmartNameRowFilter(JTable table, String query) {
        final String q = norm(query);
        final Set<String> tokens = new LinkedHashSet<>(Arrays.asList(q.split("\\s+")));

        final int firstCol = findColumn(table, "first");
        final int lastCol  = findColumn(table, "last");
        final int nameCol  = findColumn(table, "name"); // single combined name column

        return new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> e) {
                StringBuilder hay = new StringBuilder(96);

                // 1) add all textual cells (generic find-anywhere)
                TableModel m = e.getModel();
                int row = e.getIdentifier();
                for (int c = 0; c < m.getColumnCount(); c++) {
                    Object v = m.getValueAt(row, c);
                    if (v != null) hay.append(' ').append(norm(String.valueOf(v)));
                }

                // 2) add friendly name permutations
                String first = (firstCol >= 0) ? val(m, row, firstCol) : "";
                String last  = (lastCol  >= 0) ? val(m, row,  lastCol) : "";
                String name  = (nameCol  >= 0) ? val(m, row,  nameCol) : "";

                if (!first.isEmpty() || !last.isEmpty()) {
                    hay.append(' ').append(first).append(' ').append(last);       // First Last
                    hay.append(' ').append(last).append(' ').append(first);       // Last First
                    hay.append(' ').append(last).append(", ").append(first);      // Last, First
                }
                if (!name.isEmpty()) {
                    // normalise "Smith, Jake" => "smith jake"
                    String n = norm(name).replace(",", " ");
                    hay.append(' ').append(n);

                    // also add swapped "Jake Smith"
                    String[] parts = n.trim().split("\\s+");
                    if (parts.length == 2) {
                        hay.append(' ').append(parts[1]).append(' ').append(parts[0]);
                        hay.append(' ').append(parts[0]).append(", ").append(parts[1]);
                    }
                }

                String text = hay.toString();
                // AND: every token must be present somewhere (order doesn’t matter)
                for (String t : tokens) {
                    if (!t.isEmpty() && !text.contains(t)) return false;
                }
                return true;
            }

            private String val(TableModel m, int r, int c) {
                Object v = m.getValueAt(r, c);
                return (v == null) ? "" : norm(String.valueOf(v));
            }
        };
    }

    /** Find view column by header keyword (case-insensitive, partial). */
    private static int findColumn(JTable table, String needle) {
        var cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            Object h = cm.getColumn(i).getHeaderValue();
            if (h != null && norm(h.toString()).contains(needle)) return i;
        }
        return -1;
    }

    /** Lowercase; collapse punctuation to spaces (keep ' and - inside names). */
    private static String norm(String s) {
        String x = s.toLowerCase(Locale.ROOT);
        x = x.replaceAll("[\\p{Punct}&&[^'-]]", " "); // keep O'Connor, Jean-Luc friendly
        x = x.replaceAll("\\s+", " ").trim();
        return x;
    }
}
