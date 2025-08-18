package sgms.dao;

import java.util.Map;

/**
 * Grades database access. We store raw marks for each student and assignment in
 * a course.
 */
public interface GradeDAO {

    /**
     * Get all grades for one course. Map layout: studentId -> (assignmentId ->
     * raw mark).
     *
     * @param courseId the course to read
     * @return nested map of marks (can be empty)
     * @throws Exception if the database read fails
     */
    Map<Integer, Map<Integer, Integer>> findByCourse(int courseId) throws Exception;

    /**
     * Add or update one grade for a student and assignment.
     *
     * @param studentId the student
     * @param assignmentId the assignment
     * @param mark the raw mark to save
     * @throws Exception if the database write fails
     */
    void upsert(int studentId, int assignmentId, int mark) throws Exception;
}
