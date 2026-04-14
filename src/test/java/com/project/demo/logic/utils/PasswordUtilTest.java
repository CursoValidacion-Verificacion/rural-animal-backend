package com.project.demo.logic.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil")
@Tag("unit")
class PasswordUtilTest {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()\\-_+=<>?]");

    @Test
    @DisplayName("generated password is not null")
    void generateTemporalPassword_notNull() {
        assertNotNull(PasswordUtil.generateTemporalPassword());
    }

    @RepeatedTest(5)
    @DisplayName("generated password has length 9")
    void generateTemporalPassword_hasLengthNine() {
        assertEquals(9, PasswordUtil.generateTemporalPassword().length());
    }

    @RepeatedTest(5)
    @DisplayName("generated password contains at least one uppercase")
    void generateTemporalPassword_containsUppercase() {
        assertTrue(UPPERCASE.matcher(PasswordUtil.generateTemporalPassword()).find());
    }

    @RepeatedTest(5)
    @DisplayName("generated password contains at least one lowercase")
    void generateTemporalPassword_containsLowercase() {
        assertTrue(LOWERCASE.matcher(PasswordUtil.generateTemporalPassword()).find());
    }

    @RepeatedTest(5)
    @DisplayName("generated password contains at least one digit")
    void generateTemporalPassword_containsDigit() {
        assertTrue(DIGIT.matcher(PasswordUtil.generateTemporalPassword()).find());
    }

    @RepeatedTest(5)
    @DisplayName("generated password contains at least one special character")
    void generateTemporalPassword_containsSpecial() {
        assertTrue(SPECIAL.matcher(PasswordUtil.generateTemporalPassword()).find());
    }
}
