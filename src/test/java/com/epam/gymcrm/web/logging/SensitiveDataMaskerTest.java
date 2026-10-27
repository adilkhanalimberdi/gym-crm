package com.epam.gymcrm.web.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SensitiveDataMaskerTest {

    @Test
    void maskJson_hidesEveryPasswordField() {
        String json = "{\"username\":\"John\",\"oldPassword\":\"a\\\"b\",\"newPassword\":\"new\",\"password\" : \"p\"}";

        assertEquals("{\"username\":\"John\",\"oldPassword\":\"***\",\"newPassword\":\"***\",\"password\" : \"***\"}",
                SensitiveDataMasker.maskJson(json));
    }

    @Test
    void maskJson_keepsOtherFields() {
        String json = "{\"firstName\":\"Pass\",\"address\":\"Password Street\"}";

        assertEquals(json, SensitiveDataMasker.maskJson(json));
    }

    @Test
    void maskQuery_hidesPasswordParameters() {
        assertEquals("username=John&password=***&x=1", SensitiveDataMasker.maskQuery("username=John&password=secret&x=1"));
        assertEquals("password=***", SensitiveDataMasker.maskQuery("password=secret"));
    }

    @Test
    void maskQuery_keepsOtherParameters() {
        assertEquals("periodFrom=2026-11-01&trainerName=Anna", SensitiveDataMasker.maskQuery("periodFrom=2026-11-01&trainerName=Anna"));
    }
}
