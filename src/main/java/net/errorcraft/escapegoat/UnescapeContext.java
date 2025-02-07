package net.errorcraft.escapegoat;

public record UnescapeContext(boolean throwOnTrailingCodePoints, Callback callback) {
    public static final UnescapeContext DEFAULT = new UnescapeContext(true, (readCodePoints, readChars) -> {});

    @FunctionalInterface
    public interface Callback {
        void apply(int readCodePoints, int readChars);
    }
}
