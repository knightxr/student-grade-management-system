package sgms.service;

import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationServiceTest {

    @Test
    public void testIsNonEmpty() {
        assertTrue(ValidationService.isNonEmpty("abc"));
        assertFalse(ValidationService.isNonEmpty(""));
        assertFalse(ValidationService.isNonEmpty("   "));
    }

    @Test
    public void testIsName() {
        assertTrue(ValidationService.isName("Anne-Marie Smith"));
        assertFalse(ValidationService.isName("John1"));
        assertFalse(ValidationService.isName(""));
    }

    @Test
    public void testIsIntInRange() {
        assertTrue(ValidationService.isIntInRange(5, 0, 10));
        assertFalse(ValidationService.isIntInRange(-1, 0, 10));
        assertFalse(ValidationService.isIntInRange(11, 0, 10));
    }

    @Test
    public void testIsUsername() {
        assertTrue(ValidationService.isUsername("user_01"));
        assertFalse(ValidationService.isUsername("ab"));
        assertFalse(ValidationService.isUsername("user!"));
    }

    @Test
    public void testIsStrongPassword() {
        assertTrue(ValidationService.isStrongPassword("abc12345"));
        assertFalse(ValidationService.isStrongPassword("abcdefg"));
        assertFalse(ValidationService.isStrongPassword("12345678"));
        assertFalse(ValidationService.isStrongPassword("a1b2"));
    }
}