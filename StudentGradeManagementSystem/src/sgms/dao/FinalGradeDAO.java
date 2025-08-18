package sgms.dao;

import java.util.List;
import sgms.model.FinalGrade;

/**
 * Final grade database access. Lets us read final grades and the list of grade
 * levels that have data.
 */
public interface FinalGradeDAO {

    /**
     * Get all final grades for one grade level.
     *
     * @param gradeLevel e.g. 8, 9, 10, 11, 12
     * @return list of final grades (can be empty)
     * @throws Exception if the database read fails
     */
    List<FinalGrade> findByGradeLevel(int gradeLevel) throws Exception;

    /**
     * Get all grade levels that exist in the final grades table.
     *
     * @return list of grade levels (distinct)
     * @throws Exception if the database read fails
     */
    List<Integer> findGradeLevels() throws Exception;
}
