package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PreferSurrounderCharacterEscapeRuleTest {
    private static final int FIRST_ESCAPE_CODE_POINT = 'a';
    private static final String FIRST_ESCAPE_CODE_POINT_AS_STRING = Character.toString(FIRST_ESCAPE_CODE_POINT);
    private static final String FIRST_ESCAPED = "escaped_a";
    private static final int SECOND_ESCAPE_CODE_POINT = 'b';
    private static final String SECOND_ESCAPED = "escaped_b";

    private static final PreferSurrounderCharacterEscapeRule TEST_RULE = PreferSurrounderCharacterEscapeRule.builder()
        .add(FIRST_ESCAPE_CODE_POINT, FIRST_ESCAPED)
        .add(SECOND_ESCAPE_CODE_POINT, SECOND_ESCAPED)
        .build();

    @Test
    void escapedEscapeWithSameSurrounderCodePointReturnsEscapedValue() {
        String[] escaped = TEST_RULE.escaped(FIRST_ESCAPE_CODE_POINT, FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNotNull(escaped, "Rule should escape the specified code point");
        Assertions.assertEquals(1, escaped.length, "Rule should return exactly one escaped string");
        Assertions.assertEquals(FIRST_ESCAPED, escaped[0], "Rule should return the specified escaped string");
    }

    @Test
    void escapedEscapeWithOtherSurrounderCodePointReturnsNull() {
        String[] escaped = TEST_RULE.escaped(SECOND_ESCAPE_CODE_POINT, FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void escapedEscapeWithoutSurrounderCodePointReturnsNull() {
        String[] escaped = TEST_RULE.escaped(FIRST_ESCAPE_CODE_POINT, null);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void escapedEscapeWithUnknownCodePointReturnsNull() {
        String[] escaped = TEST_RULE.escaped('x', FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void escapedEscapeWithUnknownSurrounderCodePointReturnsNull() {
        String[] escaped = TEST_RULE.escaped('x', (int) 'x');
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void unescapedUnescapeWithSameSurrounderCodePointReturnsUnescapedValue() {
        CodePointReader reader = new CodePointReader(FIRST_ESCAPED);
        String unescaped = TEST_RULE.unescaped(reader, FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNotNull(unescaped, "Rule should unescape the specified code point");
        Assertions.assertEquals(FIRST_ESCAPE_CODE_POINT_AS_STRING, unescaped, "Rule should return the specified unescaped string");
    }

    @Test
    void unescapedUnescapeWithOtherSurrounderCodePointReturnsNull() {
        CodePointReader reader = new CodePointReader(SECOND_ESCAPED);
        String unescaped = TEST_RULE.unescaped(reader, FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified code point");
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void unescapedUnescapeWithoutSurrounderCodePointReturnsNull() {
        CodePointReader reader = new CodePointReader(FIRST_ESCAPED);
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified code point");
    }

    @Test
    void unescapedUnescapeWithUnknownStringReturnsNull() {
        CodePointReader reader = new CodePointReader("x");
        String unescaped = TEST_RULE.unescaped(reader, FIRST_ESCAPE_CODE_POINT);
        Assertions.assertNull(unescaped, "Rule should not escape the specified code point");
    }

    @Test
    void unescapedUnescapeWithUnknownSurrounderCodePointReturnsNull() {
        CodePointReader reader = new CodePointReader("x");
        String unescaped = TEST_RULE.unescaped(reader, (int) 'x');
        Assertions.assertNull(unescaped, "Rule should not escape the specified code point");
    }

    @Test
    void shouldBeEscapedWithSameSurrounderCodePointReturnsTrue() {
        Assertions.assertTrue(TEST_RULE.shouldBeEscaped(FIRST_ESCAPE_CODE_POINT, FIRST_ESCAPE_CODE_POINT), "Rule should escape the specified code point");
    }

    @Test
    void shouldBeEscapedWithOtherSurrounderCodePointReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped(SECOND_ESCAPE_CODE_POINT, FIRST_ESCAPE_CODE_POINT), "Rule should not escape the specified code point");
    }

    @Test
    @SuppressWarnings("ConstantValue")
    void shouldBeEscapedWithoutSurrounderCodePointReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped(SECOND_ESCAPE_CODE_POINT, null), "Rule should not escape the specified code point");
    }

    @Test
    void shouldBeEscapedWithUnknownCodePointReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped('x', FIRST_ESCAPE_CODE_POINT), "Rule should not escape the specified code point");
    }

    @Test
    void shouldBeEscapedWithUnknownSurrounderCodePointReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped('x', (int) 'x'), "Rule should not escape the specified code point");
    }
}
