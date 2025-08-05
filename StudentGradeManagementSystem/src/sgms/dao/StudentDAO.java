package sgms.dao;

import sgms.model.Student;
import sgms.model.Course;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {

    Student add(Student s) throws Exception;           // returns obj with new ID
    boolean update(Student s) throws Exception;        // true if row updated
    boolean delete(int studentId) throws Exception;
    Optional<Student> findById(int studentId) throws Exception;
    List<Student> findAll() throws Exception;
    
    /** Returns all students in the given grade level. */
    List<Student> findByGradeLevel(int gradeLevel) throws Exception;

    /** Returns the distinct grade levels that currently exist in the DB. */
    List<Integer> findGradeLevels() throws Exception;

    /** Returns the distinct courses that currently exist in the DB. */
    List<Course> findCourses() throws Exception;

    /** Returns all students enrolled in the given course. */
    List<Student> findByCourse(int courseId) throws Exception;
}