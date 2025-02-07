package net.errorcraft.escapegoat;

import net.errorcraft.escapegoat.rule.CharacterEscapeRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringEscaperTest {
    public static final StringEscaper TEST_STRING_ESCAPER = StringEscaper.builder('a', "a")
        .suffix('b')
        .surrounder('\'', "'")
        .rule(CharacterEscapeRule.ofAlwaysEscape('x', "x"))
        .rule(CharacterEscapeRule.ofOptionalEscape('y', "y"))
        .build();

    public static final StringEscaper TEST_STRING_ESCAPER_WITH_ESCAPED_SUFFIX = StringEscaper.builder('a', "a")
        .suffix('b')
        .surrounder('\'', "'")
        .rule(CharacterEscapeRule.ofAlwaysEscape('b', "b"))
        .build();

    @Test
    void escapeWithEscapePrefixAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escape("a");
        Assertions.assertEquals("'aab'", result);
    }

    @Test
    void escapeWithSurrounderAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escape("'");
        Assertions.assertEquals("'a'b'", result);
    }

    @Test
    void escapeWithEscapeSuffixAsInputShouldNotEscapeValue() {
        String result = TEST_STRING_ESCAPER.escape("b");
        Assertions.assertEquals("'b'", result);
    }

    @Test
    void escapeWithEscapeSuffixAsInputShouldEscapeValueIfSpecified() {
        String result = TEST_STRING_ESCAPER_WITH_ESCAPED_SUFFIX.escape("b");
        Assertions.assertEquals("'abb'", result);
    }

    @Test
    void escapeWithValidRuleAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escape("x");
        Assertions.assertEquals("'axb'", result);
    }

    @Test
    void escapeWithInvalidRuleAsInputShouldNotEscapeValue() {
        String result = TEST_STRING_ESCAPER.escape("z");
        Assertions.assertEquals("'z'", result);
    }

    @Test
    void escapeCodePointWithEscapePrefixAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escapeCodePoint('a');
        Assertions.assertEquals("aab", result);
    }

    @Test
    void escapeCodePointWithSurrounderAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escapeCodePoint('\'');
        Assertions.assertEquals("a'b", result);
    }

    @Test
    void escapeCodePointWithEscapeSuffixAsInputShouldNotEscapeValue() {
        String result = TEST_STRING_ESCAPER.escapeCodePoint('b');
        Assertions.assertEquals("b", result);
    }

    @Test
    void escapeCodePointWithEscapeSuffixAsInputShouldEscapeValueIfSpecified() {
        String result = TEST_STRING_ESCAPER_WITH_ESCAPED_SUFFIX.escapeCodePoint('b');
        Assertions.assertEquals("abb", result);
    }

    @Test
    void escapeCodePointWithValidRuleAsInputShouldEscapeValue() {
        String result = TEST_STRING_ESCAPER.escapeCodePoint('x');
        Assertions.assertEquals("axb", result);
    }

    @Test
    void escapeCodePointWithInvalidRuleAsInputShouldNotEscapeValue() {
        String result = TEST_STRING_ESCAPER.escapeCodePoint('z');
        Assertions.assertEquals("z", result);
    }

    @Test
    void unescapeWithValidInputUnescapesString() {
        String result = TEST_STRING_ESCAPER.unescape("'aab a'b axb y z'");
        Assertions.assertEquals("a ' x y z", result);
    }

    @Test
    void unescapeWithNoSurrounderAtStartOfStringThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("zzz"));
    }

    @Test
    void unescapeWithEmptyStringThatRequiresSurrounderThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape(""));
    }

    @Test
    void unescapeWithNoSurrounderAtEndOfStringThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'zzz"));
    }

    @Test
    void unescapeWithUnescapedCharacterThatShouldBeEscapedThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'x"));
    }

    @Test
    void unescapeWithInvalidEscapeCharacterThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'azb"));
    }

    @Test
    void unescapeWithUnclosedEscapeSequenceThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'ax'"));
    }

    @Test
    void unescapeWithIncompleteEscapeSequenceThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'a"));
    }

    @Test
    void unescapeWithUnclosedEscapeSequenceAtEndOfStringThrowsException() {
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'ax"));
    }

    @Test
    void unescapeWithTrailingCharactersAndDisallowedTrailingCharactersThrowsException() {
        UnescapeContext context = new UnescapeContext(true, (readCodePoints, readChars) -> {});
        Assertions.assertThrows(UnescapeStringException.class, () -> TEST_STRING_ESCAPER.unescape("'zzz'trailing", context));
    }

    @Test
    void unescapeWithTrailingCharactersAndAllowedTrailingCharactersReturnsPreviousString() {
        UnescapeContext context = new UnescapeContext(false, (readCodePoints, readChars) -> {});
        String result = Assertions.assertDoesNotThrow(() -> TEST_STRING_ESCAPER.unescape("'zzz'trailing", context));
        Assertions.assertEquals("zzz", result);
    }
}
