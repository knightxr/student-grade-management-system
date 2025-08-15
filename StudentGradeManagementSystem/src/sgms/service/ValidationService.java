package sgms.service;

import java.time.LocalDate;

/**
 * Simple validation helpers.
 */
public final class ValidationService {
    private ValidationService() {}

    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isIntInRange(int v, int min, int max) {
        return v >= min && v <= max;
    }

    public static boolean isValidCourseCode(String s) {
        return s != null && s.matches("[A-Z0-9]{2,10}");
    }

    public static boolean isValidDueDate(LocalDate due, LocalDate min) {
        return due != null && (min == null || !due.isBefore(min));
    }
}