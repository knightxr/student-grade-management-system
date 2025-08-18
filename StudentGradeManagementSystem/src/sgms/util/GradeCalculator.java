package sgms.util;

/**
 * Basic algorithms and grade maths used in the project.
 * The final grade calculation uses these TERM WEIGHTS:
 *   T1 = 12.5%, T2 = 25%, T3 = 12.5%, T4 = 50%
 * Missing terms are ignored and the remaining weights are re-normalised.
 */
public final class GradeCalculator {

    private GradeCalculator() { }

    // Term weights must add up to 100
    private static final double[] TERM_WEIGHTS = { 12.5, 25.0, 12.5, 50.0 };

    /**
     * Combine up to four term percentages (each 0..100) into a final percentage.
     * Any null term is skipped; remaining weights are re-normalised.
     *
     * @return final percent (0..100) or null if all terms are null
     */
    public static Double calculateFinalGrade(Double t1, Double t2, Double t3, Double t4) {
        Double[] terms = { t1, t2, t3, t4 };

        double weightedSum = 0.0;
        double weightTotal = 0.0;

        int i;
        for (i = 0; i < 4; i++) {
            Double term = terms[i];
            if (term != null) {
                double w = TERM_WEIGHTS[i];
                weightedSum += (term.doubleValue() * w);
                weightTotal += w;
            }
        }

        if (weightTotal <= 0.0) return null;

        double finalPct = weightedSum / weightTotal;

        // Clamp to [0, 100]
        if (finalPct < 0.0) finalPct = 0.0;
        if (finalPct > 100.0) finalPct = 100.0;

        return Double.valueOf(finalPct);
    }

    /** Average of an int array (returns double). Empty array → 0.0. */
    public static double classAverage(int[] marks) {
        if (marks == null || marks.length == 0) return 0.0;
        long total = 0;
        int i;
        for (i = 0; i < marks.length; i++) {
            total += marks[i];
        }
        return ((double) total) / marks.length;
    }

    /** Bubble sort ascending with early exit. */
    public static void bubbleSortAscending(int[] a) {
        if (a == null || a.length < 2) return;
        int n = a.length;
        boolean swapped = true;
        while (swapped) {
            swapped = false;
            int i;
            for (i = 1; i < n; i++) {
                if (a[i - 1] > a[i]) {
                    int tmp = a[i - 1];
                    a[i - 1] = a[i];
                    a[i] = tmp;
                    swapped = true;
                }
            }
            n--; // largest element moved to the end
        }
    }

    /** Linear search for an int, returns index or -1 if not found. */
    public static int linearSearch(int[] a, int target) {
        if (a == null) return -1;
        int i;
        for (i = 0; i < a.length; i++) {
            if (a[i] == target) return i;
        }
        return -1;
    }

    /** Case-insensitive linear search for a String, returns index or -1 if not found. */
    public static int linearSearch(String[] a, String target) {
        if (a == null || target == null) return -1;
        int i;
        for (i = 0; i < a.length; i++) {
            String s = a[i];
            if (s != null && s.equalsIgnoreCase(target)) return i;
        }
        return -1;
    }
}
