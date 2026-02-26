package com.project.demo.logic.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FormatterUtil")
class FormatterUtilTest {

    @Nested
    @DisplayName("formatDate")
    class FormatDate {

        @Test
        @DisplayName("returns empty string when date is null")
        void nullDate_returnsEmpty() {
            assertEquals("", FormatterUtil.formatDate(null, "dd/MM/yyyy"));
        }

        @Test
        @DisplayName("formats date with dd/MM/yyyy")
        void validDate_formatsWithDayMonthYear() {
            LocalDateTime date = LocalDateTime.of(2025, 2, 25, 10, 30);
            assertEquals("25/02/2025", FormatterUtil.formatDate(date, "dd/MM/yyyy"));
        }

        @Test
        @DisplayName("formats date with yyyy-MM-dd")
        void validDate_formatsWithIsoDate() {
            LocalDateTime date = LocalDateTime.of(2025, 12, 1, 0, 0);
            assertEquals("2025-12-01", FormatterUtil.formatDate(date, "yyyy-MM-dd"));
        }

        @Test
        @DisplayName("formats date with time pattern")
        void validDate_formatsWithTime() {
            LocalDateTime date = LocalDateTime.of(2025, 6, 15, 14, 45, 30);
            assertEquals("14:45", FormatterUtil.formatDate(date, "HH:mm"));
        }

        @Test
        @DisplayName("formats date with custom pattern")
        void validDate_formatsWithCustomPattern() {
            LocalDateTime date = LocalDateTime.of(2025, 3, 10, 9, 0);
            assertEquals("10 Mar 2025", FormatterUtil.formatDate(date, "dd MMM yyyy"));
        }
    }
}
