package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CharacterEscapeRuleTest {
    private static final int ESCAPE_CODE_POINT = 'x';
    private static final String ESCAPE_CODE_POINT_AS_STRING = Character.toString(ESCAPE_CODE_POINT);
    private static final int INVALID_ESCAPE_CODE_POINT = 'a';
    private static final String ESCAPED = "escaped";
    private static final String INVALID_ESCAPED = "invalid";
    private static final CharacterEscapeRule ALWAYS_ESCAPE_TEST_RULE = CharacterEscapeRule.ofAlwaysEscape(ESCAPE_CODE_POINT, ESCAPED);
    private static final CharacterEscapeRule OPTIONAL_ESCAPE_TEST_RULE = CharacterEscapeRule.ofOptionalEscape(ESCAPE_CODE_POINT, ESCAPED);

    @Test
    void escapedAlwaysEscapeWithValidCodePointReturnsEscapedValue() {
        String[] escaped = ALWAYS_ESCAPE_TEST_RULE.escaped(ESCAPE_CODE_POINT, null);
        Assertions.assertNotNull(escaped, "Rule should escape the specified code point");
        Assertions.assertEquals(1, escaped.length, "Rule should return exactly one escaped string");
        Assertions.assertEquals(ESCAPED, escaped[0], "Rule should return the specified escaped string");
    }

    @Test
    void escapedAlwaysEscapeWithInvalidCodePointReturnsNull() {
        String[] escaped = ALWAYS_ESCAPE_TEST_RULE.escaped(INVALID_ESCAPE_CODE_POINT, null);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void escapedOptionalEscapeWithValidCodePointReturnsNull() {
        String[] escaped = OPTIONAL_ESCAPE_TEST_RULE.escaped(ESCAPE_CODE_POINT, null);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void escapedOptionalEscapeWithInvalidCodePointReturnsNull() {
        String[] escaped = OPTIONAL_ESCAPE_TEST_RULE.escaped(INVALID_ESCAPE_CODE_POINT, null);
        Assertions.assertNull(escaped, "Rule should not escape the specified code point");
    }

    @Test
    void unescapedWithValidStringReturnsUnescapedString() {
        CodePointReader reader = new CodePointReader(ESCAPED);
        String unescaped = ALWAYS_ESCAPE_TEST_RULE.unescaped(reader, null);
        Assertions.assertEquals(ESCAPE_CODE_POINT_AS_STRING, unescaped, "Rule should return the specified target code point as a string");
    }

    @Test
    void unescapedWithInvalidStringReturnsNull() {
        CodePointReader reader = new CodePointReader(INVALID_ESCAPED);
        String unescaped = ALWAYS_ESCAPE_TEST_RULE.unescaped(reader, null);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified string");
    }

    @Test
    void shouldBeEscapedAlwaysEscapeWithValidCodePointReturnsTrue() {
        Assertions.assertTrue(ALWAYS_ESCAPE_TEST_RULE.shouldBeEscaped(ESCAPE_CODE_POINT, null), "Rule should escape the specified code point");
    }

    @Test
    void shouldBeEscapedAlwaysEscapeWithInvalidCodePointReturnsFalse() {
        Assertions.assertFalse(ALWAYS_ESCAPE_TEST_RULE.shouldBeEscaped(INVALID_ESCAPE_CODE_POINT, null), "Rule should not escape the specified code point");
    }

    @Test
    void shouldBeEscapedOptionalEscapeWithValidCodePointReturnsFalse() {
        Assertions.assertFalse(OPTIONAL_ESCAPE_TEST_RULE.shouldBeEscaped(ESCAPE_CODE_POINT, null), "Rule should not escape the specified code point");
    }

    @Test
    void shouldBeEscapedOptionalEscapeWithInvalidCodePointReturnsFalse() {
        Assertions.assertFalse(OPTIONAL_ESCAPE_TEST_RULE.shouldBeEscaped(INVALID_ESCAPE_CODE_POINT, null), "Rule should not escape the specified code point");
    }
}
