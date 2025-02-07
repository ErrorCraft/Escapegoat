package net.errorcraft.escapegoat;

public class CodePointUtil {
    private CodePointUtil() {}

    public static String toStringEllipsis(int[] codePoints, int startIndex) {
        return toStringEllipsis(codePoints, startIndex, 4);
    }

    public static String toStringEllipsis(int[] codePoints, int startIndex, int length) {
        StringBuilder builder = new StringBuilder(toString(codePoints, startIndex, length));
        if ((startIndex + length + 1) <= codePoints.length) {
            builder.append("...");
        }
        return builder.toString();
    }

    public static String toString(Integer... codePoints) {
        StringBuilder builder = new StringBuilder();
        for (Integer codePoint : codePoints) {
            if (codePoint != null) {
                builder.appendCodePoint(codePoint);
            }
        }
        return builder.toString();
    }

    public static String toString(int[] codePoints, int startIndex, int length) {
        int realStartIndex = Math.max(startIndex, 0);
        int realEndIndex = Math.min(codePoints.length - 1, startIndex + length - 1);
        StringBuilder builder = new StringBuilder();
        for (int i = realStartIndex; i <= realEndIndex; i++) {
            builder.appendCodePoint(codePoints[i]);
        }
        return builder.toString();
    }

    public static void append(StringBuilder builder, Integer codePoint) {
        if (codePoint == null) {
            return;
        }
        builder.appendCodePoint(codePoint);
    }

    public static int charCount(int[] codePoints, int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be at least 0: " + length);
        }
        if (length == 0) {
            return 0;
        }
        if (length > codePoints.length) {
            throw new IllegalArgumentException("length must be at most the length of the array: " + length);
        }
        int readChars = 0;
        for (int i = 0; i < length; i++) {
            readChars += Character.toString(codePoints[i]).length();
        }
        return readChars;
    }
}
