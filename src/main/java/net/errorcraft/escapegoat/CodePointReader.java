package net.errorcraft.escapegoat;

import org.jetbrains.annotations.Nullable;

import java.util.function.IntPredicate;

public class CodePointReader {
    private final int[] codePoints;
    private int index;

    public CodePointReader(String value) {
        this(value, 0);
    }

    public CodePointReader(String value, int index) {
        this.codePoints = value.codePoints().toArray();
        this.index = index;
    }

    public int index() {
        return this.index;
    }

    public void index(int index) {
        this.index = index;
    }

    public boolean trySkipNext(@Nullable String value) {
        if (value == null) {
            return true;
        }
        int[] valueCodePoints = value.codePoints().toArray();
        if (!this.canRead(valueCodePoints.length)) {
            return false;
        }
        for (int i = 0; i < valueCodePoints.length; i++) {
            if (valueCodePoints[i] != this.codePoints[this.index + i]) {
                return false;
            }
        }
        this.index += valueCodePoints.length;
        return true;
    }

    public String read(IntPredicate isValidCodePoint, int minLength, int maxLength) throws UnescapeStringException {
        int foundCodePoints = 0;
        StringBuilder builder = new StringBuilder();
        while (foundCodePoints < maxLength && this.canRead(foundCodePoints + 1) && isValidCodePoint.test(this.codePoints[this.index + foundCodePoints])) {
            builder.appendCodePoint(this.codePoints[this.index + foundCodePoints]);
            foundCodePoints++;
        }
        if (foundCodePoints < minLength) {
            throw new UnescapeStringException("Sequence must be at least " + minLength + " code points long, got " + foundCodePoints + " instead: " + builder);
        }
        this.index += foundCodePoints;
        return builder.toString();
    }

    private boolean canRead(int codePointAmount) {
        return this.index + codePointAmount <= this.codePoints.length;
    }
}
