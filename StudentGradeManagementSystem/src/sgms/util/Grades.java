package sgms.util;

/**
 * Maps numeric averages to grade symbols.<br>
 * Order: A ({@literal >=} 80), B ({@literal >=} 70), C ({@literal >=} 60),
 * D ({@literal >=} 50), E ({@literal >=} 40), F ({@literal >=} 30), G ({@literal <} 30).
 */
public final class Grades {
    private Grades() {}

    public static final int[] CUTS = {80, 70, 60, 50, 40, 30, 0};
    public static final char[] SYMBOLS = {'A', 'B', 'C', 'D', 'E', 'F', 'G'};

    public static char symbolFor(int average) {
        for (int i = 0; i < CUTS.length; i++) {
            if (average >= CUTS[i]) {
                return SYMBOLS[i];
            }
        }
        return SYMBOLS[SYMBOLS.length - 1];
    }
}