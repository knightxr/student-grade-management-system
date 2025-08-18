package sgms.dao;

import java.util.List;
import sgms.model.Assignment;

/**
 * Assignment database access. We add, change, remove and list assignments for a
 * course.
 */
public interface AssignmentDAO {

    /**
     * Add a new assignment and return it with its new ID.
     *
     * @param a assignment to save
     * @return saved assignment (now has an ID)
     * @throws Exception if the database write fails
     */
    Assignment add(Assignment a) throws Exception;

    /**
     * Update an existing assignment.
     *
     * @param a assignment with new values
     * @throws Exception if the database write fails
     */
    void update(Assignment a) throws Exception;

    /**
     * Delete one assignment by ID.
     *
     * @param assignmentId the ID to remove
     * @return true if a row was deleted, false if not
     * @throws Exception if the delete fails
     */
    boolean delete(int assignmentId) throws Exception;

    /**
     * Get all assignments for one course.
     *
     * @param courseId the course to look up
     * @return list of assignments (can be empty)
     * @throws Exception if the database read fails
     */
    List<Assignment> findByCourse(int courseId) throws Exception;
}
