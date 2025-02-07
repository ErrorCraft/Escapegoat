package net.errorcraft.escapegoat;

import net.errorcraft.escapegoat.rule.CharacterEscapeRule;
import net.errorcraft.escapegoat.rule.UnicodeEscapeRule;

public class StringEscapers {
    public static final StringEscaper JSON = StringEscaper.builder('\\', "\\")
        .surrounder('"', "\"")
        .rule(CharacterEscapeRule.ofOptionalEscape('/', "/"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\b', "b"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\f', "f"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\n', "n"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\r', "r"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\t', "t"))
        .rule(UnicodeEscapeRule.builder(Character::isISOControl)
            .prefix("u")
            .transformation(UnicodeEscapeRule.Transformation.UTF16)
            .format(UnicodeEscapeRule.Format.HEXADECIMAL)
            .length(4)
            .build())
        .build();
    public static final StringEscaper JAVA = StringEscaper.builder('\\', "\\")
        .surrounder('"', "\"")
        .rule(CharacterEscapeRule.ofOptionalEscape('\'', "'"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\b', "b"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\f', "f"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\n', "n"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\r', "r"))
        .rule(CharacterEscapeRule.ofAlwaysEscape('\t', "t"))
        .rule(CharacterEscapeRule.ofOptionalEscape(' ', "s"))
        .rule(UnicodeEscapeRule.builder(Character::isISOControl)
            .prefix("u")
            .transformation(UnicodeEscapeRule.Transformation.UTF16)
            .format(UnicodeEscapeRule.Format.HEXADECIMAL)
            .length(4)
            .build())
        .rule(UnicodeEscapeRule.builder(codePoint -> false)
            .transformation(UnicodeEscapeRule.Transformation.UTF8)
            .format(UnicodeEscapeRule.Format.OCTAL)
            .minLength(1)
            .maxLength(3)
            .maxCodePoint(255)
            .build())
        .build();
    public static final StringEscaper SNBT = StringEscaper.builder('\\', "\\")
        .surrounder('\'', "'")
        .surrounder('"', "\"")
        .strictSurroundEscape()
        .build();

    private StringEscapers() {}
}
