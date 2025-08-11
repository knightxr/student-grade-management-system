package sgms.dao;

import java.util.Map;

/** DAO interface for managing student feedback notes. */
public interface FeedbackDAO {
    /** Returns map of studentId -> comment for students in given course. */
    Map<Integer, String> findByCourse(int courseId) throws Exception;

    /** Inserts or updates feedback for a student in a course. */
    void upsert(int studentId, int courseId, String note) throws Exception;

    /** Deletes feedback for a student in a course. */
    void delete(int studentId, int courseId) throws Exception;
}