package sgms.util;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests for {@link GradeCalculator}. */
public class GradeCalculatorTest {

    @Test
    public void weightedFinalUsesSpecifiedWeights() {
        Double grade = GradeCalculator.calculateFinalGrade(80.0, 70.0, 90.0, 60.0);
        assertEquals(68.75, grade.doubleValue(), 0.001);
    }

    @Test
    public void renormalisesWhenOnlyT4Present() {
        Double grade = GradeCalculator.calculateFinalGrade(null, null, null, 77.0);
        assertEquals(77.0, grade.doubleValue(), 0.001);
    }

    @Test
    public void clampsAboveHundred() {
        Double grade = GradeCalculator.calculateFinalGrade(150.0, 160.0, 170.0, 180.0);
        assertEquals(100.0, grade.doubleValue(), 0.001);
    }

    @Test
    public void clampsBelowZero() {
        Double grade = GradeCalculator.calculateFinalGrade(-20.0, -10.0, -30.0, -40.0);
        assertEquals(0.0, grade.doubleValue(), 0.001);
    }
}