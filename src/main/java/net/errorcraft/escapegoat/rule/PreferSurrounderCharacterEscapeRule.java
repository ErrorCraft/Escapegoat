package net.errorcraft.escapegoat.rule;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.errorcraft.escapegoat.CodePointReader;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PreferSurrounderCharacterEscapeRule(Int2ObjectMap<String> codePoints) implements EscapeRule {
    public static PreferSurrounderCharacterEscapeRule of(Int2ObjectMap<String> codePoints) {
        return new PreferSurrounderCharacterEscapeRule(codePoints);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String @Nullable [] escaped(int codePoint, Integer surrounderCodePoint) {
        if (surrounderCodePoint != null && codePoint == surrounderCodePoint && this.codePoints.containsKey(codePoint)) {
            return new String[] { this.codePoints.get(codePoint) };
        }
        return null;
    }

    @Override
    public @Nullable String unescaped(CodePointReader reader, Integer surrounderCodePoint) {
        if (surrounderCodePoint != null && this.codePoints.containsKey(surrounderCodePoint.intValue()) && reader.trySkipNext(this.codePoints.get(surrounderCodePoint.intValue()))) {
            return Character.toString(surrounderCodePoint);
        }
        return null;
    }

    @Override
    public boolean shouldBeEscaped(int codePoint, Integer surrounderCodePoint) {
        return surrounderCodePoint != null && codePoint == surrounderCodePoint && this.codePoints.containsKey(codePoint);
    }

    public static class Builder {
        private final Int2ObjectArrayMap<String> codePoints = new Int2ObjectArrayMap<>();

        private Builder() {}

        public PreferSurrounderCharacterEscapeRule build() {
            return PreferSurrounderCharacterEscapeRule.of(this.codePoints);
        }

        public Builder add(int codePoint, String value) {
            this.codePoints.put(codePoint, Objects.requireNonNull(value));
            return this;
        }
    }
}
