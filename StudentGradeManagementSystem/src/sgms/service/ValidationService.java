package sgms.service;

import java.time.LocalDate;

/**
 * Small set of checks we use around the app.
 * Kept simple and easy to read.
 */
public final class ValidationService {

    private ValidationService() {
        // no objects of this class
    }

    /** True if the string is not null and not just spaces. */
    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** Check an int is between min and max (both included). */
    public static boolean isIntInRange(int v, int min, int max) {
        return v >= min && v <= max;
    }

    /**
     * Name check: only letters, spaces, or hyphens.
     * e.g. "Amy-Jane Smith" is OK.
     */
    public static boolean isName(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '-') {
                return false;
            }
        }
        return true;
    }

    /**
     * Username check: 3-20 chars, only letters, digits, or underscore.
     */
    public static boolean isUsername(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len < 3 || len > 20) return false;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    /**
     * Password check: at least 8 characters, must have a letter and a digit.
     */
    public static boolean isStrongPassword(String s) {
        if (s == null || s.length() < 8) return false;

        boolean hasLetter = false;
        boolean hasDigit  = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }

    /**
     * Course code check without regex: 2..10 chars, only A-Z or 0-9.
     * e.g. "IT12" is OK. Lowercase letters are NOT allowed here.
     */
    public static boolean isValidCourseCode(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len < 2 || len > 10) return false;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            boolean isUpperAZ = (c >= 'A' && c <= 'Z');
            boolean isDigit   = Character.isDigit(c);
            if (!isUpperAZ && !isDigit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Due date must not be before the given minimum date.
     * If min is null, any non-null due date is fine.
     */
    public static boolean isValidDueDate(LocalDate due, LocalDate min) {
        if (due == null) return false;
        if (min == null) return true;
        return !due.isBefore(min);
    }
}
