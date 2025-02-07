package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import net.errorcraft.escapegoat.UnescapeStringException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnicodeEscapeRuleTest {
    private static final String ESCAPE_PREFIX = "PREFIX";
    private static final String ESCAPE_SUFFIX = "SUFFIX";

    private static final int BMP_ESCAPE_CODE_POINT = 'x';
    private static final String BMP_ESCAPE_CODE_POINT_AS_STRING = Character.toString(BMP_ESCAPE_CODE_POINT);
    private static final String BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_ESCAPE_SEQUENCE = "0078";
    private static final String BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_STRING = ESCAPE_PREFIX + BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_ESCAPE_SEQUENCE + ESCAPE_SUFFIX;

    // Character: 😊
    private static final int SMP_ESCAPE_CODE_POINT = 0x1F60A;
    private static final String[] SMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_STRINGS = {
        ESCAPE_PREFIX + "d83d" + ESCAPE_SUFFIX,
        ESCAPE_PREFIX + "de0a" + ESCAPE_SUFFIX
    };

    private static final int INVALID_ESCAPE_CODE_POINT = 'a';
    private static final int MAX_CODE_POINT = 0xE000;
    private static final String ABOVE_MAX_CODE_POINT = "E001";

    private static final String ESCAPED = ESCAPE_PREFIX + BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_ESCAPE_SEQUENCE + ESCAPE_SUFFIX;
    private static final String INVALID_ESCAPED_NO_PREFIX = BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_ESCAPE_SEQUENCE + ESCAPE_SUFFIX;
    private static final String INVALID_ESCAPED_NO_SUFFIX = ESCAPE_PREFIX + BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_ESCAPE_SEQUENCE;
    private static final String INVALID_ESCAPED_INCORRECT_LENGTH = ESCAPE_PREFIX + "0" + ESCAPE_SUFFIX;
    private static final String INVALID_ESCAPED_CODE_POINT_TOO_LARGE = ESCAPE_PREFIX + ABOVE_MAX_CODE_POINT + ESCAPE_SUFFIX;

    private static final UnicodeEscapeRule TEST_RULE = UnicodeEscapeRule.builder(codePoint -> codePoint == BMP_ESCAPE_CODE_POINT || codePoint == SMP_ESCAPE_CODE_POINT)
        .prefix(ESCAPE_PREFIX)
        .suffix(ESCAPE_SUFFIX)
        .transformation(UnicodeEscapeRule.Transformation.UTF16)
        .format(UnicodeEscapeRule.Format.HEXADECIMAL)
        .maxCodePoint(MAX_CODE_POINT)
        .length(4)
        .build();

    @Test
    void escapedWithSimpleCodePointPassingPredicateReturnsEscapedValue() {
        String[] escaped = TEST_RULE.escaped(BMP_ESCAPE_CODE_POINT, null);
        Assertions.assertNotNull(escaped, "Rule should escape the specified code point");
        Assertions.assertEquals(1, escaped.length, "Rule should return exactly one escaped string");
        Assertions.assertEquals(BMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_STRING, escaped[0], "Rule should return the prefix, escaped string and suffix joined");
    }

    @Test
    void escapedWithComplicatedCodePointPassingPredicateReturnsEscapedValue() {
        String[] escaped = TEST_RULE.escaped(SMP_ESCAPE_CODE_POINT, null);
        Assertions.assertNotNull(escaped, "Rule should escape the specified code point");
        Assertions.assertEquals(2, escaped.length, "Rule should return exactly two escaped strings");
        Assertions.assertArrayEquals(SMP_ESCAPE_CODE_POINT_AS_UTF16_HEX_STRINGS, escaped, "Rule should return the prefix, escaped string and suffix joined");
    }

    @Test
    void escapedWithCodePointNotPassingPredicateReturnsNull() {
        String[] escaped = TEST_RULE.escaped(INVALID_ESCAPE_CODE_POINT, null);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void unescapedWithValidStringReturnsUnescapedString() {
        CodePointReader reader = new CodePointReader(ESCAPED);
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertEquals(BMP_ESCAPE_CODE_POINT_AS_STRING, unescaped, "Rule should return the specified target UTF-16 character as a string");
    }

    @Test
    void unescapedWithInvalidStringWithNoPrefixReturnsNull() {
        CodePointReader reader = new CodePointReader(INVALID_ESCAPED_NO_PREFIX);
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified string");
    }

    @Test
    void unescapedWithInvalidStringWithNoSuffixReturnsNull() {
        CodePointReader reader = new CodePointReader(INVALID_ESCAPED_NO_SUFFIX);
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified string");
    }

    @Test
    void unescapedWithInvalidStringWithIncorrectLengthThrowsException() {
        CodePointReader reader = new CodePointReader(INVALID_ESCAPED_INCORRECT_LENGTH);
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_RULE.unescaped(reader, null), "Rule should throw an exception for an invalid string length");
    }

    @Test
    void unescapedWithInvalidStringWithTooLargeCodePointReturnsNull() {
        CodePointReader reader = new CodePointReader(INVALID_ESCAPED_CODE_POINT_TOO_LARGE);
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_RULE.unescaped(reader, null), "Rule should throw an exception for reading a code point that is too big");
    }

    @Test
    void shouldBeEscapedWithCodePointPassingPredicateReturnsTrue() {
        Assertions.assertTrue(TEST_RULE.shouldBeEscaped(BMP_ESCAPE_CODE_POINT, null), "Rule should escape the specified code point");
    }

    @Test
    void shouldBeEscapedWithCodePointNotPassingPredicateReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped(INVALID_ESCAPE_CODE_POINT, null), "Rule should not escape the specified code point");
    }

    @Nested
    class TransformationTest {
        private static final int BMP_ESCAPE_CODE_POINT = 'x';
        private static final String[] BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF8_ARRAY = new String[] { "78" };
        private static final String[] BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF16_ARRAY = new String[] { "78" };
        private static final String[] BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF32_ARRAY = new String[] { "78" };

        // Character: 😊
        private static final int SMP_ESCAPE_CODE_POINT = 0x1F60A;
        private static final String[] SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF8_ARRAY = new String[] { "f0", "9f", "98", "8a" };
        private static final String[] SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF16_ARRAY = new String[] { "d83d", "de0a" };
        private static final String[] SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF32_ARRAY = new String[] { "1f60a" };

        @Test
        void escapeUtf8WithSimpleCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF8.escape(BMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF8_ARRAY, escaped, "UTF-8 Transformation should escape the character correctly");
        }

        @Test
        void escapeUtf16WithSimpleCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF16.escape(BMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF16_ARRAY, escaped, "UTF-16 Transformation should escape the character correctly");
        }

        @Test
        void escapeUtf32WithSimpleCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF32.escape(BMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(BMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF32_ARRAY, escaped, "UTF-32 Transformation should escape the character correctly");
        }

        @Test
        void escapeUtf8WithComplicatedCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF8.escape(SMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF8_ARRAY, escaped, "UTF-8 Transformation should escape the character correctly");
        }

        @Test
        void escapeUtf16WithComplicatedCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF16.escape(SMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF16_ARRAY, escaped, "UTF-16 Transformation should escape the character correctly");
        }

        @Test
        void escapeUtf32WithComplicatedCodePointEscapesCorrectly() {
            String[] escaped = UnicodeEscapeRule.Transformation.UTF32.escape(SMP_ESCAPE_CODE_POINT, UnicodeEscapeRule.Format.HEXADECIMAL);
            Assertions.assertArrayEquals(SMP_ESCAPE_CODE_POINT_AS_STRINGIFIED_UTF32_ARRAY, escaped, "UTF-32 Transformation should escape the character correctly");
        }

        @Test
        void unescapeUtf8UnescapesCorrectly() {
            String unescaped = UnicodeEscapeRule.Transformation.UTF8.unescape(BMP_ESCAPE_CODE_POINT);
            Assertions.assertEquals("x", unescaped, "UTF-8 Transformation should unescape the input correctly");
        }

        @Test
        void unescapeUtf16UnescapesCorrectly() {
            String unescaped = UnicodeEscapeRule.Transformation.UTF16.unescape(BMP_ESCAPE_CODE_POINT);
            Assertions.assertEquals("x", unescaped, "UTF-16 Transformation should unescape the input correctly");
        }
        @Test
        void unescapeUtf32UnescapesCorrectly() {
            String unescaped = UnicodeEscapeRule.Transformation.UTF32.unescape(SMP_ESCAPE_CODE_POINT);
            Assertions.assertEquals("😊", unescaped, "UTF-32 Transformation should unescape the input correctly");
        }
    }

    @Nested
    class Format {
        @Test
        void escapeBinaryConvertsCorrectly() {
            String escaped = UnicodeEscapeRule.Format.BINARY.escape(0b1100100);
            Assertions.assertEquals("1100100", escaped, "Binary Format should escape the input correctly");
        }

        @Test
        void escapeOctalConvertsCorrectly() {
            String escaped = UnicodeEscapeRule.Format.OCTAL.escape(100);
            Assertions.assertEquals("144", escaped, "Octal Format should escape the input correctly");
        }

        @Test
        void escapeDecimalConvertsCorrectly() {
            String escaped = UnicodeEscapeRule.Format.DECIMAL.escape(100);
            Assertions.assertEquals("100", escaped, "Decimal Format should escape the input correctly");
        }

        @Test
        void escapeHexadecimalConvertsCorrectly() {
            String escaped = UnicodeEscapeRule.Format.HEXADECIMAL.escape(0x64);
            Assertions.assertEquals("64", escaped, "Hexadecimal Format should escape the input correctly");
        }

        @Test
        void readBinaryConvertsCorrectly() {
            CodePointReader reader = new CodePointReader("1100100zzz");
            int read = UnicodeEscapeRule.Format.BINARY.read(reader, 0, Integer.MAX_VALUE);
            Assertions.assertEquals(100, read, "Binary Format should read the string correctly");
        }

        @Test
        void readOctalConvertsCorrectly() {
            CodePointReader reader = new CodePointReader("144zzz");
            int read = UnicodeEscapeRule.Format.OCTAL.read(reader, 0, Integer.MAX_VALUE);
            Assertions.assertEquals(100, read, "Octal Format should read the string correctly");
        }

        @Test
        void readDecimalConvertsCorrectly() {
            CodePointReader reader = new CodePointReader("100zzz");
            int read = UnicodeEscapeRule.Format.DECIMAL.read(reader, 0, Integer.MAX_VALUE);
            Assertions.assertEquals(100, read, "Decimal Format should read the string correctly");
        }

        @Test
        void readHexadecimalConvertsCorrectly() {
            CodePointReader reader = new CodePointReader("64zzz");
            int read = UnicodeEscapeRule.Format.HEXADECIMAL.read(reader, 0, Integer.MAX_VALUE);
            Assertions.assertEquals(100, read, "Hexadecimal Format should read the string correctly");
        }
    }
}
