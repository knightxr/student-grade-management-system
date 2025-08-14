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
}