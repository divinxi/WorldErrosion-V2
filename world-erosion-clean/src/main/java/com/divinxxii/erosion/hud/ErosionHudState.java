package com.divinxxii.erosion.hud;

import com.divinxxii.erosion.ErosionManager;

public class ErosionHudState {
    public static volatile int stateOrdinal     = ErosionManager.State.STOPPED.ordinal();
    public static volatile int secondsRemaining = 0;
    public static volatile int intervalSeconds  = 0;

    public static void update(int state, int seconds, int interval) {
        stateOrdinal     = state;
        secondsRemaining = seconds;
        intervalSeconds  = interval;
    }

    public static ErosionManager.State getState() {
        return ErosionManager.State.values()[stateOrdinal];
    }
}
