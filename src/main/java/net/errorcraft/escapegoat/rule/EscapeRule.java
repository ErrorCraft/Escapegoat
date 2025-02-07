package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import net.errorcraft.escapegoat.UnescapeStringException;
import org.jetbrains.annotations.Nullable;

public interface EscapeRule {
    String @Nullable [] escaped(int codePoint, Integer surrounderCodePoint);
    @Nullable String unescaped(CodePointReader reader, Integer surrounderCodePoint) throws UnescapeStringException;
    boolean shouldBeEscaped(int codePoint, Integer surrounderCodePoint);
}
