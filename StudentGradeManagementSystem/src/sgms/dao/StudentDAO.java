package sgms.dao;

import sgms.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentDAO {

    Student add(Student s) throws Exception;           // returns obj with new ID
    boolean update(Student s) throws Exception;        // true if row updated
    boolean delete(int studentId) throws Exception;
    Optional<Student> findById(int studentId) throws Exception;
    List<Student> findAll() throws Exception;
}