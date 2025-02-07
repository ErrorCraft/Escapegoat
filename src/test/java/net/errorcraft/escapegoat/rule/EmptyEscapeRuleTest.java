package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmptyEscapeRuleTest {
    private static final EmptyEscapeRule TEST_RULE = EmptyEscapeRule.of("\n");

    @Test
    void escapedAlwaysReturnsNull() {
        String[] escaped = TEST_RULE.escaped('\n', null);
        Assertions.assertNull(escaped, "Rule should never escape the specified code point");
    }

    @Test
    void unescapedWithValidStringReturnsEmptyString() {
        CodePointReader reader = new CodePointReader("\n");
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertEquals("", unescaped, "Rule should return an empty string");
    }

    @Test
    void unescapedWithInvalidStringReturnsNull() {
        CodePointReader reader = new CodePointReader("invalid");
        String unescaped = TEST_RULE.unescaped(reader, null);
        Assertions.assertNull(unescaped, "Rule should not unescape the specified string");
    }

    @Test
    void shouldBeEscapedAlwaysReturnsFalse() {
        Assertions.assertFalse(TEST_RULE.shouldBeEscaped('\n', null), "Rule should never escape the specified code point");
    }
}
