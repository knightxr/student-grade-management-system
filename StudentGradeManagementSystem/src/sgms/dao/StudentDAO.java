package sgms.dao;

import java.util.List;
import sgms.model.Course;
import sgms.model.Student;

/**
 * Student database access. We can add, update, delete, list students, and
 * manage course enrolments.
 */
public interface StudentDAO {

    /**
     * Add a new student and return it with its new ID.
     *
     * @param s the student to save
     * @return the saved student (now has an ID)
     * @throws Exception if the database write fails
     */
    Student add(Student s) throws Exception;

    /**
     * Update an existing student.
     *
     * @param s student with new values
     * @return true if a row was updated, false if not
     * @throws Exception if the database write fails
     */
    boolean update(Student s) throws Exception;

    /**
     * Delete one student by ID.
     *
     * @param studentId the ID to remove
     * @return true if a row was deleted, false if not
     * @throws Exception if the delete fails
     */
    boolean delete(int studentId) throws Exception;

    /**
     * Find one student by ID.
     *
     * @param studentId the ID to look up
     * @return the student, or null if not found
     * @throws Exception if the database read fails
     */
    Student findById(int studentId) throws Exception;

    /**
     * Get all students.
     *
     * @return list of students (can be empty)
     * @throws Exception if the database read fails
     */
    List<Student> findAll() throws Exception;

    /**
     * Get all students in one grade level.
     *
     * @param gradeLevel e.g. 8, 9, 10, 11, 12
     * @return list of students (can be empty)
     * @throws Exception if the database read fails
     */
    List<Student> findByGradeLevel(int gradeLevel) throws Exception;

    /**
     * Get all grade levels that currently have students.
     *
     * @return list of distinct grade levels
     * @throws Exception if the database read fails
     */
    List<Integer> findGradeLevels() throws Exception;

    /**
     * Get all courses that exist.
     *
     * @return list of courses (can be empty)
     * @throws Exception if the database read fails
     */
    List<Course> findCourses() throws Exception;

    /**
     * Link a student to a course (enrol).
     *
     * @param studentId the student
     * @param courseId the course
     * @return true if a row was inserted, false if already enrolled or no
     * change
     * @throws Exception if the database write fails
     */
    boolean enrollStudentInCourse(int studentId, int courseId) throws Exception;

    /**
     * Get all students who are enrolled in one course.
     *
     * @param courseId the course
     * @return list of students (can be empty)
     * @throws Exception if the database read fails
     */
    List<Student> findByCourse(int courseId) throws Exception;

    /**
     * Remove the link between a student and a course (unenrol).
     *
     * @param studentId the student
     * @param courseId the course
     * @return true if a row was deleted, false if no link existed
     * @throws Exception if the database write fails
     */
    boolean removeStudentFromCourse(int studentId, int courseId) throws Exception;
}
