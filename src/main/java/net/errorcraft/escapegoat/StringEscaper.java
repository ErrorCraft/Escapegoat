package net.errorcraft.escapegoat;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.errorcraft.escapegoat.rule.CharacterEscapeRule;
import net.errorcraft.escapegoat.rule.EscapeRule;
import net.errorcraft.escapegoat.rule.PreferSurrounderCharacterEscapeRule;
import net.errorcraft.escapegoat.rule.UnicodeEscapeRule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StringEscaper {
    // Escaper to prevent people from breaking error messages if they decide to use control characters for surrounders or escape sequences for some reason
    private static final StringEscaper EXCEPTION_STRING_ESCAPER = StringEscaper.builder('\\', "\\")
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

    private final int[] surrounderCodePoints;
    private final int escapePrefixCodePoint;
    private final Integer escapeSuffixCodePoint;
    private final EscapeRule[] escapeRules;

    private StringEscaper(int[] surrounderCodePoints, int escapePrefixCodePoint, Integer escapeSuffixCodePoint, EscapeRule[] escapeRules) {
        this.surrounderCodePoints = surrounderCodePoints;
        this.escapePrefixCodePoint = escapePrefixCodePoint;
        this.escapeSuffixCodePoint = escapeSuffixCodePoint;
        this.escapeRules = escapeRules;
    }

    public static Builder builder(int escapePrefixCodePoint, String escapedPrefix) {
        return new Builder(escapePrefixCodePoint, escapedPrefix);
    }

    public String escape(String value) {
        Integer surrounderCodePoint = this.surrounderCodePoint();
        StringBuilder builder = new StringBuilder();
        CodePointUtil.append(builder, surrounderCodePoint);
        for (int codePoint : value.codePoints().toArray()) {
            builder.append(this.escapeIfNecessary(codePoint, surrounderCodePoint));
        }
        CodePointUtil.append(builder, surrounderCodePoint);
        return builder.toString();
    }

    public String escapeCodePoint(int codePoint) {
        return this.escapeIfNecessary(codePoint, null);
    }

    public String unescape(String value) throws UnescapeStringException {
        return this.unescape(value, UnescapeContext.DEFAULT);
    }

    public String unescape(String value, UnescapeContext context) throws UnescapeStringException {
        StringBuilder builder = new StringBuilder();
        EscapeState state = EscapeState.START_STRING;
        int[] codePoints = value.codePoints().toArray();
        Integer surrounder = null;
        int i = 0;
        for (; i < codePoints.length; i++) {
            int codePoint = codePoints[i];
            switch (state) {
                case START_STRING -> {
                    surrounder = this.surrounderCodePoint(codePoint);
                    state = EscapeState.NONE;
                    if (surrounder != null) {
                        continue;
                    }
                }
                case START_ESCAPE -> {
                    CodePointReader reader = new CodePointReader(value, i);
                    builder.append(this.unescape(reader, surrounder, codePoint, i));
                    i = reader.index() - 1;
                    state = this.escapeSuffixCodePoint == null ? EscapeState.NONE : EscapeState.END_ESCAPE;
                    continue;
                }
                case END_ESCAPE -> {
                    if (codePoint == this.escapeSuffixCodePoint) {
                        state = EscapeState.NONE;
                        continue;
                    }
                    throw new UnescapeStringException("Expected " + EXCEPTION_STRING_ESCAPER.escapeCodePoint(this.escapeSuffixCodePoint) + " with code point " + codePoint + " to end an escape sequence at position " + i + ": " + CodePointUtil.toStringEllipsis(codePoints, i));
                }
            }
            if (codePoint == this.escapePrefixCodePoint) {
                state = EscapeState.START_ESCAPE;
                continue;
            }
            if (surrounder != null && codePoint == surrounder) {
                state = EscapeState.END_STRING;
                break;
            }
            if (this.shouldBeEscaped(codePoint, surrounder)) {
                throw new UnescapeStringException("Character " + EXCEPTION_STRING_ESCAPER.escapeCodePoint(codePoint) + " with code point " + codePoint + " should be escaped at position " + i + ": " + CodePointUtil.toStringEllipsis(codePoints, i));
            }
            builder.appendCodePoint(codePoint);
        }
        if (context.throwOnTrailingCodePoints() && (i + 1) < codePoints.length) {
            throw new UnescapeStringException("Trailing characters found in string at code point " + (i + 1) + ": " + CodePointUtil.toStringEllipsis(codePoints, i + 1));
        }
        return switch (state) {
            case START_STRING -> {
                if (this.surrounderCodePoints.length > 0) {
                    throw new UnescapeStringException("Expected surrounder to start a string");
                }
                context.callback().apply(0, 0);
                yield "";
            }
            case END_STRING -> {
                int readChars = CodePointUtil.charCount(codePoints, i + 1);
                context.callback().apply(i, readChars);
                yield builder.toString();
            }
            case NONE -> {
                if (surrounder != null) {
                    throw new UnescapeStringException("Unclosed string, expected " + EXCEPTION_STRING_ESCAPER.escapeCodePoint(surrounder) + " to close the string");
                }
                int readChars = CodePointUtil.charCount(codePoints, i + 1);
                context.callback().apply(i, readChars);
                yield builder.toString();
            }
            case START_ESCAPE -> throw new UnescapeStringException("Ended string with an incomplete escape sequence");
            case END_ESCAPE -> throw new UnescapeStringException("Expected " + EXCEPTION_STRING_ESCAPER.escapeCodePoint(this.escapeSuffixCodePoint) + " to end an escape sequence at end of string");
        };
    }

    private Integer surrounderCodePoint() {
        if (this.surrounderCodePoints.length == 0) {
            return null;
        }
        return this.surrounderCodePoints[0];
    }

    private Integer surrounderCodePoint(int codePoint) throws UnescapeStringException {
        if (this.surrounderCodePoints.length == 0) {
            return null;
        }
        for (int surrounderCodePoint : this.surrounderCodePoints) {
            if (surrounderCodePoint == codePoint) {
                return codePoint;
            }
        }
        throw new UnescapeStringException("Expected surrounder to start a string");
    }

    private String unescape(CodePointReader reader, Integer surrounder, int codePoint, int index) throws UnescapeStringException {
        for (EscapeRule escapeRule : this.escapeRules) {
            @Nullable String unescaped = escapeRule.unescaped(reader, surrounder);
            if (unescaped == null) {
                continue;
            }
            return unescaped;
        }
        throw new UnescapeStringException("Invalid escape sequence " + CodePointUtil.toString(this.escapePrefixCodePoint, codePoint, this.escapeSuffixCodePoint) + " at position " + index);
    }

    private String escapeIfNecessary(int codePoint, Integer surrounderCodePoint) {
        for (EscapeRule escapeRule : this.escapeRules) {
            String @Nullable [] escaped = escapeRule.escaped(codePoint, surrounderCodePoint);
            if (escaped == null) {
                continue;
            }
            StringBuilder builder = new StringBuilder();
            for (String escapedValue : escaped) {
                builder.appendCodePoint(this.escapePrefixCodePoint);
                builder.append(escapedValue);
                CodePointUtil.append(builder, this.escapeSuffixCodePoint);
            }
            return builder.toString();
        }
        return Character.toString(codePoint);
    }

    private boolean shouldBeEscaped(int codePoint, Integer surrounder) {
        for (EscapeRule escapeRule : this.escapeRules) {
            if (escapeRule.shouldBeEscaped(codePoint, surrounder)) {
                return true;
            }
        }
        return false;
    }

    private enum EscapeState {
        START_STRING,
        END_STRING,
        NONE,
        START_ESCAPE,
        END_ESCAPE
    }

    public static class Builder {
        private final Int2ObjectArrayMap<String> surrounderCodePoints = new Int2ObjectArrayMap<>();
        private final int escapePrefixCodePoint;
        private final String escapePrefixString;
        private Integer escapeSuffixCodePoint;
        private final List<EscapeRule> escapeRules = new ArrayList<>();
        private boolean strictSurroundEscape;

        private Builder(int escapePrefixCodePoint, String escapePrefixString) {
            this.escapePrefixCodePoint = escapePrefixCodePoint;
            this.escapePrefixString = escapePrefixString;
        }

        public StringEscaper build() {
            List<EscapeRule> allEscapeRules = new ArrayList<>();
            if (this.strictSurroundEscape) {
                allEscapeRules.add(PreferSurrounderCharacterEscapeRule.of(this.surrounderCodePoints));
            } else {
                this.surrounderCodePoints.forEach((codePoint, escaped) -> allEscapeRules.add(CharacterEscapeRule.ofAlwaysEscape(codePoint, escaped)));
            }
            allEscapeRules.add(CharacterEscapeRule.ofAlwaysEscape(this.escapePrefixCodePoint, this.escapePrefixString));
            allEscapeRules.addAll(this.escapeRules);
            return new StringEscaper(this.surrounderCodePoints.keySet().toIntArray(), this.escapePrefixCodePoint, this.escapeSuffixCodePoint, allEscapeRules.toArray(EscapeRule[]::new));
        }

        public Builder suffix(int codePoint) {
            this.escapeSuffixCodePoint = codePoint;
            return this;
        }

        public Builder strictSurroundEscape() {
            this.strictSurroundEscape = true;
            return this;
        }

        public Builder surrounder(int codePoint, String escaped) {
            this.surrounderCodePoints.put(codePoint, Objects.requireNonNull(escaped));
            return this;
        }

        public Builder rule(EscapeRule escapeRule) {
            this.escapeRules.add(Objects.requireNonNull(escapeRule));
            return this;
        }
    }
}
