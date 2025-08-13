package sgms.dao;

import sgms.model.Assignment;
import java.util.List;

public interface AssignmentDAO {
    Assignment add(Assignment a) throws Exception;
    void update(Assignment a) throws Exception;
    boolean delete(int assignmentId) throws Exception;
    List<Assignment> findByCourse(int courseId) throws Exception;
}