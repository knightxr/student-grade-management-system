package sgms.dao;

import java.util.Map;

/**
 * Feedback data access for a course. We store short notes for each student in a
 * course.
 */
public interface FeedbackDAO {

    /**
     * Get all feedback notes for one course. The map key is the studentId, and
     * the value is the note text.
     *
     * @param courseId the course we are looking at
     * @return map of studentId -> note (empty if none)
     * @throws Exception if the database read fails
     */
    Map<Integer, String> findByCourse(int courseId) throws Exception;

    /**
     * Add a new note or update an existing one for a student in a course.
     *
     * @param studentId the student
     * @param courseId the course
     * @param note the text to save (use empty string to clear)
     * @throws Exception if the database write fails
     */
    void upsert(int studentId, int courseId, String note) throws Exception;

    /**
     * Remove a student's note for a course.
     *
     * @param studentId the student
     * @param courseId the course
     * @throws Exception if the delete fails
     */
    void delete(int studentId, int courseId) throws Exception;
}
