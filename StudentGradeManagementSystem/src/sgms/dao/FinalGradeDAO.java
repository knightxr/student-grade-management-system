package sgms.dao;

import java.util.List;
import sgms.model.FinalGrade;

public interface FinalGradeDAO {
    List<FinalGrade> findByGradeLevel(int gradeLevel) throws Exception;
    List<Integer> findGradeLevels() throws Exception;
}