package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record CharacterEscapeRule(int targetCodePoint, String escaped, boolean alwaysEscape) implements EscapeRule {
    public static CharacterEscapeRule ofOptionalEscape(int targetCodePoint, String escaped) {
        return new CharacterEscapeRule(targetCodePoint, Objects.requireNonNull(escaped), false);
    }

    public static CharacterEscapeRule ofAlwaysEscape(int targetCodePoint, String escaped) {
        return new CharacterEscapeRule(targetCodePoint, Objects.requireNonNull(escaped), true);
    }

    @Override
    public String @Nullable [] escaped(int codePoint, Integer surrounderCodePoint) {
        if (this.alwaysEscape && codePoint == this.targetCodePoint) {
            return new String[] { this.escaped };
        }
        return null;
    }

    @Override
    public @Nullable String unescaped(CodePointReader reader, Integer surrounderCodePoint) {
        if (reader.trySkipNext(this.escaped)) {
            return Character.toString(this.targetCodePoint);
        }
        return null;
    }

    @Override
    public boolean shouldBeEscaped(int codePoint, Integer surrounderCodePoint) {
        return codePoint == this.targetCodePoint && this.alwaysEscape;
    }
}
