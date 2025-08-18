package sgms.dao;

import java.util.List;
import sgms.model.Course;

/**
 * Course database access. We can list, add, update, delete, and find by code.
 */
public interface CourseDAO {

    /**
     * Get all courses for a grade level.
     *
     * @param gradeLevel e.g. 8, 9, 10, 11, 12
     * @return list of courses (can be empty)
     * @throws Exception if the database read fails
     */
    List<Course> findByGrade(int gradeLevel) throws Exception;

    /**
     * Add a new course.
     *
     * @param c course to add
     * @return saved course with its new ID
     * @throws Exception if the database write fails
     */
    Course add(Course c) throws Exception;

    /**
     * Update a course.
     *
     * @param c course with new values
     * @return true if a row changed, false if not
     * @throws Exception if the database write fails
     */
    boolean update(Course c) throws Exception;

    /**
     * Delete a course by ID.
     *
     * @param courseId ID to remove
     * @return true if a row was deleted, false if not
     * @throws Exception if the delete fails
     */
    boolean delete(int courseId) throws Exception;

    /**
     * Find one course by its code.
     *
     * @param code unique course code
     * @return the course, or null if not found
     * @throws Exception if the database read fails
     */
    Course findByCode(String code) throws Exception;
}
