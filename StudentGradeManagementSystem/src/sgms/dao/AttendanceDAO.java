package sgms.dao;

import java.time.LocalDate;
import java.util.Map;

/**
 * Attendance database access. Stores if a student was present on a date for a
 * course.
 */
public interface AttendanceDAO {

    /**
     * Get attendance for all students in a course between two dates. Map
     * layout: studentId -> (date -> present).
     *
     * @param courseId course to check
     * @param start start date (included)
     * @param end end date (included)
     * @return map of studentId to their date/present values
     * @throws Exception if the database read fails
     */
    Map<Integer, Map<LocalDate, Boolean>> findByCourseAndDateRange(
            int courseId, LocalDate start, LocalDate end) throws Exception;

    /**
     * Add or update one attendance record.
     *
     * @param studentId student
     * @param courseId course
     * @param date day of the class
     * @param present true if present, false if absent
     * @throws Exception if the database write fails
     */
    void upsert(int studentId, int courseId, LocalDate date, boolean present) throws Exception;
}
