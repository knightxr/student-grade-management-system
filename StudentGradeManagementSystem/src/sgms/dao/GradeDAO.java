package sgms.dao;

import java.util.Map;

public interface GradeDAO {
    /**
     * Returns grades for all students in the given course.
     * Map key: studentId -> (assignmentId -> mark)
     */
    Map<Integer, Map<Integer, Integer>> findByCourse(int courseId) throws Exception;

    /** Updates or inserts a grade for the given student/assignment pair. */
    void upsert(int studentId, int assignmentId, int mark) throws Exception;
}