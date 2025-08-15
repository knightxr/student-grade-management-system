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

    /** Returns true if the string consists of letters, spaces or hyphens. */
    public static boolean isName(String s) {
        if (s == null || s.trim().isEmpty()) return false;
        int i;
        for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetter(c) && c != ' ' && c != '-') {
                return false;
            }
        }
        return true;
    }

    /** Username: 3-20 letters, digits or underscore. */
    public static boolean isUsername(String s) {
        if (s == null) return false;
        int len = s.length();
        if (len < 3 || len > 20) return false;
        int i;
        for (i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    /** Strong password: >=8 chars, contains at least one letter and one digit. */
    public static boolean isStrongPassword(String s) {
        if (s == null) return false;
        if (s.length() < 8) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;
        int i;
        for (i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) hasLetter = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }
    
    public static boolean isValidCourseCode(String s) {
        return s != null && s.matches("[A-Z0-9]{2,10}");
    }

    public static boolean isValidDueDate(LocalDate due, LocalDate min) {
        return due != null && (min == null || !due.isBefore(min));
    }
}