package sgms.dao;

import java.util.List;
import sgms.model.Course;

/**
 * DAO for manipulating courses.
 */
public interface CourseDAO {

    /** Returns all courses for the given grade level including student counts. */
    List<Course> findByGrade(int gradeLevel) throws Exception;

    /** Inserts a new course and returns it with the generated ID. */
    Course add(Course c) throws Exception;

    /** Updates the name/code of a course. */
    boolean update(Course c) throws Exception;

    /** Deletes the course with the given ID. */
    boolean delete(int courseId) throws Exception;
}