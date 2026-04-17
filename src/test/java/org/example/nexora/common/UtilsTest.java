package org.example.nexora.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @Test
    void testGenerateId() {
        String id = Utils.generateId();
        assertNotNull(id);
    }

    @Test
    void testValidateEmail() {
        assertTrue(Utils.isValidEmail("test@example.com"));
        assertFalse(Utils.isValidEmail("invalid"));
    }

    @Test
    void testFormatDate() {
        String formatted = Utils.formatDate(java.time.LocalDate.now());
        assertNotNull(formatted);
    }
}