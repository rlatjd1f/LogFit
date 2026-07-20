package com.logfit.app;

public final class TimerState {
    private TimerState() {}

    public static int remainingSeconds(long endAtMillis, long nowMillis) {
        return Math.max(0, (int) Math.ceil((endAtMillis - nowMillis) / 1000.0));
    }
}
