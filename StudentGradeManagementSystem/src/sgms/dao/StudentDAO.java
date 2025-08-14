package sgms.dao;

import sgms.model.Student;
import sgms.model.Course;
import java.util.List;

public interface StudentDAO {

    Student add(Student s) throws Exception;           // returns object with new ID
    boolean update(Student s) throws Exception;        // true if row updated
    boolean delete(int studentId) throws Exception;
    Student findById(int studentId) throws Exception;  // null if not found
    List<Student> findAll() throws Exception;

    // all students in a grade
    List<Student> findByGradeLevel(int gradeLevel) throws Exception;

    // all distinct grade levels currently in the database
    List<Integer> findGradeLevels() throws Exception;

    // all courses available
    List<Course> findCourses() throws Exception;

    // link a student to a course
    boolean enrollStudentInCourse(int studentId, int courseId) throws Exception;

    // students in a specific course
    List<Student> findByCourse(int courseId) throws Exception;

    // remove link between a student and a course
    boolean removeStudentFromCourse(int studentId, int courseId) throws Exception;
}