package pl.adixuyt.kitauto;

/**
 * Tracks the time since the last successful beetroot-soup use.
 * Repeated uses are allowed again after one second, even while the use button
 * remains held.
 */
public final class UseCooldownState {
    private static final long SOUP_COOLDOWN_NANOS = 1_000_000_000L;

    private static long lastSuccessfulSoupUseNanos;

    private UseCooldownState() {
    }

    public static boolean isSoupCoolingDown() {
        long lastUse = lastSuccessfulSoupUseNanos;
        return lastUse != 0L && System.nanoTime() - lastUse < SOUP_COOLDOWN_NANOS;
    }

    public static void recordSuccessfulSoupUse() {
        lastSuccessfulSoupUseNanos = System.nanoTime();
    }
}
