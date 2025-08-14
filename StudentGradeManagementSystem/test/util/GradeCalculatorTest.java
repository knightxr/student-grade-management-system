package sgms.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link GradeCalculator}.
 */
public class GradeCalculatorTest {

    @Test
    public void calculatesWeightedFinalGrade() {
        Double grade = GradeCalculator.calculateFinalGrade(80.0, 70.0, 90.0, 60.0);
        // 80*0.125 + 70*0.25 + 90*0.125 + 60*0.5 = 68.75
        assertEquals(68.75, grade, 0.0001);
    }

    @Test
    public void capsValueToHundred() {
        Double grade = GradeCalculator.calculateFinalGrade(120.0, 110.0, 130.0, 140.0);
        assertEquals(100.0, grade, 0.0001);
    }

    @Test
    public void returnsNullWhenNoTermsPresent() {
        assertNull(GradeCalculator.calculateFinalGrade(null, null, null, null));
    }

    @Test
    public void normalizesWeightsWhenTermsMissing() {
        Double grade = GradeCalculator.calculateFinalGrade(80.0, null, null, null);
        assertEquals(80.0, grade, 0.0001);
    }
}