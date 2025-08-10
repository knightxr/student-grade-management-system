package sgms.dao;

import java.time.LocalDate;
import java.util.Map;

/**
 * DAO interface for managing student attendance records.
 */
public interface AttendanceDAO {
    /**
     * Returns attendance map for students in the given course between start and end dates.
     * Map key: studentId -> (date -> present)
     */
    Map<Integer, Map<LocalDate, Boolean>> findByCourseAndDateRange(int courseId, LocalDate start, LocalDate end) throws Exception;

    /**
     * Inserts or updates a single attendance entry.
     */
    void upsert(int studentId, int courseId, LocalDate date, boolean present) throws Exception;
}