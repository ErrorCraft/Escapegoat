package net.errorcraft.escapegoat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CodePointReaderTest {
    @Test
    void trySkipNextWithValidStringSkipsValue() {
        CodePointReader reader = new CodePointReader("abc123");
        boolean result = reader.trySkipNext("abc");
        Assertions.assertTrue(result, "Reader must return true for skipping the specified string");
        Assertions.assertEquals(3, reader.index(), "Reader must advance the amount of code points present in the specified string");
    }

    @Test
    void trySkipNextWithInvalidStringDoesNotSkipValue() {
        CodePointReader reader = new CodePointReader("abc123");
        boolean result = reader.trySkipNext("invalid");
        Assertions.assertFalse(result, "Reader must return false for not skipping the specified string");
        Assertions.assertEquals(0, reader.index(), "Reader must not advance any code points");
    }

    @Test
    void readWithValidStringReturnsValue() {
        CodePointReader reader = new CodePointReader("aaabc");
        String result = reader.read(codePoint -> codePoint == 'a', 0, Integer.MAX_VALUE);
        Assertions.assertEquals("aaa", result, "Reader must return the matched string");
        Assertions.assertEquals(3, reader.index(), "Reader must advance the amount of code points matched");
    }

    @Test
    void readWithInvalidStringReturnsEmptyString() {
        CodePointReader reader = new CodePointReader("123");
        String result = reader.read(codePoint -> codePoint == 'a', 0, Integer.MAX_VALUE);
        Assertions.assertEquals("", result, "Reader must return an empty string");
        Assertions.assertEquals(0, reader.index(), "Reader must not advance any code points");
    }

    @Test
    void readWithTooShortStringThrowsException() {
        CodePointReader reader = new CodePointReader("aaa");
        Assertions.assertThrows(UnescapeStringException.class, () -> reader.read(codePoint -> codePoint == 'a', 5, Integer.MAX_VALUE), "Reader should throw an exception for a too short string");
        Assertions.assertEquals(0, reader.index(), "Reader must not advance any code points");
    }

    @Test
    void readWithTooLongStringStopsEarly() {
        CodePointReader reader = new CodePointReader("aaa");
        String result = reader.read(codePoint -> codePoint == 'a', 0, 2);
        Assertions.assertEquals("aa", result, "Reader must return a string with the maximum amount of code points");
        Assertions.assertEquals(2, reader.index(), "Reader must advance the specified maximum amount of code points");
    }
}
