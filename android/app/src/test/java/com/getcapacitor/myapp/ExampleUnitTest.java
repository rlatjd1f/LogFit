package com.logfit.app;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void remainingSecondsRoundsUpAndNeverBecomesNegative() {
        assertEquals(3, TimerState.remainingSeconds(12_500L, 10_000L));
        assertEquals(0, TimerState.remainingSeconds(9_000L, 10_000L));
    }
}
