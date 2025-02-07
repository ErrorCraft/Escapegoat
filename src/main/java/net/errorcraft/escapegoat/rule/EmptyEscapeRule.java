package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import net.errorcraft.escapegoat.UnescapeStringException;
import org.jetbrains.annotations.Nullable;

public record EmptyEscapeRule(String escaped) implements EscapeRule {
    public static EmptyEscapeRule of(String escaped) {
        return new EmptyEscapeRule(escaped);
    }

    @Override
    public String @Nullable [] escaped(int codePoint, Integer surrounderCodePoint) {
        return null;
    }

    @Override
    public @Nullable String unescaped(CodePointReader reader, Integer surrounderCodePoint) throws UnescapeStringException {
        if (reader.trySkipNext(this.escaped)) {
            return "";
        }
        return null;
    }

    @Override
    public boolean shouldBeEscaped(int codePoint, Integer surrounderCodePoint) {
        return false;
    }
}
