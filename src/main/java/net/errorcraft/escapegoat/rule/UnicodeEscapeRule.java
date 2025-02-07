package net.errorcraft.escapegoat.rule;

import net.errorcraft.escapegoat.CodePointReader;
import net.errorcraft.escapegoat.UnescapeStringException;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.IntPredicate;

public record UnicodeEscapeRule(String prefix, String suffix, Transformation transformation, Format format, int minLength, int maxLength, int maxCodePoint, IntPredicate isForcedCodePoint) implements EscapeRule {
    public UnicodeEscapeRule {
        if (maxCodePoint > Character.MAX_CODE_POINT) {
            throw new IllegalArgumentException("Code point " + maxCodePoint + " is above the maximum: " + Character.MAX_CODE_POINT);
        }
        if (maxCodePoint < Character.MIN_CODE_POINT) {
            throw new IllegalArgumentException("Code point " + maxCodePoint + " is below the minimum: " + Character.MIN_CODE_POINT);
        }
        if (minLength < 1) {
            throw new IllegalArgumentException("Minimum length must be at least 1: " + minLength);
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("Maximum length must be at least the minimum length: " + maxLength);
        }
    }
    public static Builder builder(IntPredicate isForcedCodePoint) {
        return new Builder(isForcedCodePoint);
    }

    @Override
    public String @Nullable [] escaped(int codePoint, Integer surrounderCodePoint) {
        if (this.isForcedCodePoint == null) {
            return null;
        }
        if (this.isForcedCodePoint.test(codePoint)) {
            return this.escapeAndSplit(codePoint);
        }
        return null;
    }

    @Override
    public @Nullable String unescaped(CodePointReader reader, Integer surrounderCodePoint) throws UnescapeStringException {
        int start = reader.index();
        if (!reader.trySkipNext(this.prefix)) {
            return null;
        }
        int read = this.format.read(reader, this.minLength, this.maxLength);
        if (read > this.maxCodePoint) {
            throw new UnescapeStringException("Code point must be at most " + this.maxCodePoint + ": " + read);
        }
        if (!reader.trySkipNext(this.suffix)) {
            reader.index(start);
            return null;
        }
        return this.transformation.unescape(read);
    }

    @Override
    public boolean shouldBeEscaped(int codePoint, Integer surrounderCodePoint) {
        return this.isForcedCodePoint.test(codePoint);
    }

    private String[] escapeAndSplit(int codePoint) {
        String[] result = this.transformation.escape(codePoint, this.format);
        for (int i = 0; i < result.length; i++) {
            StringBuilder builder = new StringBuilder();
            if (this.prefix != null) {
                builder.append(this.prefix);
            }
            builder.append(this.padStringIfNecessary(result[i]));
            if (this.suffix != null) {
                builder.append(this.suffix);
            }
            result[i] = builder.toString();
        }
        return result;
    }

    private String padStringIfNecessary(String value) {
        int length = value.length();
        if (length >= this.minLength) {
            return value;
        }
        return "0".repeat(this.minLength - length) + value;
    }

    public enum Transformation {
        UTF8(codePoint -> {
            byte[] bytes = Character.toString(codePoint).getBytes(StandardCharsets.UTF_8);
            int[] result = new int[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = bytes[i];
                if (bytes[i] < 0) {
                    result[i] += 256;
                }
            }
            return result;
        }, value -> Character.toString((char) value)),
        UTF16(codePoint -> {
            char[] chars = Character.toString(codePoint).toCharArray();
            int[] result = new int[chars.length];
            for (int i = 0; i < chars.length; i++) {
                result[i] = chars[i];
            }
            return result;
        }, value -> Character.toString((char) value)),
        UTF32(codePoint -> new int[] { codePoint }, Character::toString);

        private final CodepointEscaper escaper;
        private final CodepointUnescaper unescaper;

        Transformation(CodepointEscaper escaper, CodepointUnescaper unescaper) {
            this.escaper = escaper;
            this.unescaper = unescaper;
        }

        public String[] escape(int codePoint, Format format) {
            int[] entries = this.escaper.escape(codePoint);
            String[] escaped = new String[entries.length];
            for (int i = 0; i < entries.length; i++) {
                escaped[i] = format.escape(entries[i]);
            }
            return escaped;
        }

        public String unescape(int value) {
            return this.unescaper.unescape(value);
        }

        @FunctionalInterface
        private interface CodepointEscaper {
            int[] escape(int codePoint);
        }

        @FunctionalInterface
        private interface CodepointUnescaper {
            String unescape(int value);
        }
    }

    public enum Format {
        BINARY(2, codePoint -> codePoint == '0' || codePoint == '1'),
        OCTAL(8, codePoint -> codePoint >= '0' && codePoint <= '7'),
        DECIMAL(10, codePoint -> codePoint >= '0' && codePoint <= '9'),
        HEXADECIMAL(16, codePoint -> (codePoint >= '0' && codePoint <= '9') || (codePoint >= 'a' && codePoint <= 'f') || (codePoint >= 'A' && codePoint <= 'F'));

        private final int radix;
        private final IntPredicate isValidCodePoint;

        Format(int radix, IntPredicate isValidCodePoint) {
            this.radix = radix;
            this.isValidCodePoint = isValidCodePoint;
        }

        public String escape(int value) {
            return Integer.toString(value, this.radix);
        }

        public int read(CodePointReader reader, int minLength, int maxLength) throws UnescapeStringException {
            String read = reader.read(this.isValidCodePoint, minLength, maxLength);
            return Integer.parseInt(read, this.radix);
        }
    }

    public static class Builder {
        private String prefix;
        private String suffix;
        private Transformation transformation = Transformation.UTF32;
        private Format format = Format.DECIMAL;
        private int minLength = 1;
        private int maxLength = Integer.MAX_VALUE;
        private int maxCodePoint = Character.MAX_CODE_POINT;
        private final IntPredicate isForcedCodePoint;

        public Builder(IntPredicate isForcedCodePoint) {
            this.isForcedCodePoint = isForcedCodePoint;
        }

        public UnicodeEscapeRule build() {
            return new UnicodeEscapeRule(this.prefix, this.suffix, this.transformation, this.format, this.minLength, this.maxLength, this.maxCodePoint, this.isForcedCodePoint);
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder transformation(Transformation transformation) {
            this.transformation = transformation;
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder length(int length) {
            this.minLength = this.maxLength = length;
            return this;
        }

        public Builder minLength(int length) {
            this.minLength = length;
            return this;
        }

        public Builder maxLength(int length) {
            this.maxLength = length;
            return this;
        }

        public Builder maxCodePoint(int maxCodePoint) {
            if (maxCodePoint > Character.MAX_CODE_POINT) {
                throw new IllegalArgumentException("Code point " + maxCodePoint + " is above the maximum: " + Character.MAX_CODE_POINT);
            }
            if (maxCodePoint < Character.MIN_CODE_POINT) {
                throw new IllegalArgumentException("Code point " + maxCodePoint + " is below the minimum: " + Character.MIN_CODE_POINT);
            }
            this.maxCodePoint = maxCodePoint;
            return this;
        }
    }
}
