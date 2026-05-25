package com.divinxxii.erosion.hud;

import com.divinxxii.erosion.ErosionManager;

/**
 * Holds the latest erosion state on the client side,
 * received via network packets from the server.
 *
 * Thread-safe via volatile fields (written on render thread, read on render thread).
 */
public class ErosionHudState {

    public static volatile int  stateOrdinal     = ErosionManager.State.STOPPED.ordinal();
    public static volatile int  secondsRemaining = 0;
    public static volatile int  intervalSeconds  = 0;

    public static void update(int state, int seconds, int interval) {
        stateOrdinal     = state;
        secondsRemaining = seconds;
        intervalSeconds  = interval;
    }

    public static ErosionManager.State getState() {
        return ErosionManager.State.values()[stateOrdinal];
    }
}
