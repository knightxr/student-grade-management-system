package sgms.util;

/**
 * Utility for calculating weighted final grades based on term averages.
 */
public final class GradeCalculator {

    private GradeCalculator() {
    }

    /**
     * Calculates the weighted final grade using the same weights as the
     * "View Final Grades" feature:
     *
     * <ul>
     *   <li>Term&nbsp;1 – 12.5&nbsp;%</li>
     *   <li>Term&nbsp;2 – 25&nbsp;%</li>
     *   <li>Term&nbsp;3 – 12.5&nbsp;%</li>
     *   <li>Term&nbsp;4 – 50&nbsp;%</li>
     * </ul>
     *
     * Terms without any assignments are ignored and the remaining weights are
     * scaled proportionally so that missing terms do not unfairly lower the
     * final grade. The result is capped in the inclusive range 0–100.
     * If all term values are {@code null}, this method returns {@code null}.
     */
    public static Double calculateFinalGrade(Double term1, Double term2,
            Double term3, Double term4) {
        double total = 0.0;
        double weight = 0.0;
        if (term1 != null) {
            total += term1 * 0.125;
            weight += 0.125;
        }
        if (term2 != null) {
            total += term2 * 0.25;
            weight += 0.25;
        }
        if (term3 != null) {
            total += term3 * 0.125;
            weight += 0.125;
        }
        if (term4 != null) {
            total += term4 * 0.5;
            weight += 0.5;
        }
        if (weight == 0.0) {
            return null;
        }
        double result = total / weight;
        return Math.max(0.0, Math.min(result, 100.0));
    }

    /** Returns the class average for the given marks. */
    public static double classAverage(int[] marks) {
        if (marks == null || marks.length == 0) {
            return 0.0;
        }
        int total = 0;
        for (int i = 0; i < marks.length; i++) {
            total += marks[i];
        }
        return (double) total / marks.length;
    }

    /** Simple bubble sort that orders the array ascending with early exit. */
    public static void bubbleSortAscending(int[] a) {
        boolean swapped;
        for (int i = 0; i < a.length - 1; i++) {
            swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /** Linear search for integers. Returns index or -1. */
    public static int linearSearch(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == target) {
                return i;
            }
        }
        return -1;
    }

    /** Case-insensitive linear search for strings. Returns index or -1. */
    public static int linearSearch(String[] a, String target) {
        if (target == null) {
            return -1;
        }
        for (int i = 0; i < a.length; i++) {
            if (target.equalsIgnoreCase(a[i])) {
                return i;
            }
        }
        return -1;
    }
}