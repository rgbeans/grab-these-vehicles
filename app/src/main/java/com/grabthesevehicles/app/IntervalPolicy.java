package com.grabthesevehicles.app;

import java.util.Random;

/** Pure scheduling policy; no dependency on the Android runtime. */
public final class IntervalPolicy {
    public static final int DEFAULT_MIN = 30;
    public static final int DEFAULT_MAX = 60;
    public static final int LIMIT_MIN = 15;
    public static final int LIMIT_MAX = 1440;
    private IntervalPolicy() {}

    public static boolean valid(int min, int max) {
        return min >= LIMIT_MIN && max <= LIMIT_MAX && min <= max;
    }

    public static long delayMillis(int min, int max, Random random) {
        if (!valid(min, max)) throw new IllegalArgumentException("Use 15–1440 minutes, with minimum ≤ maximum.");
        long floor = min * 60_000L;
        long range = (max - min) * 60_000L;
        return floor + (range == 0 ? 0 : (long) (random.nextDouble() * (range + 1)));
    }

    public static int[] freshBag(int size, int previous, Random random) {
        if (size < 2) throw new IllegalArgumentException("At least two messages are required.");
        int[] bag = new int[size];
        for (int i = 0; i < size; i++) bag[i] = i;
        for (int i = size - 1; i > 0; i--) {
            int j = random.nextInt(i + 1), old = bag[i];
            bag[i] = bag[j]; bag[j] = old;
        }
        if (bag[0] == previous) {
            int j = 1 + random.nextInt(size - 1), old = bag[0];
            bag[0] = bag[j]; bag[j] = old;
        }
        return bag;
    }
}
