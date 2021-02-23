package com.sm.supere.thread;

import android.util.Log;

public class CommonThread extends Thread {

    public static final String TAG = "CommonThread";

    @Override
    public void run() {
        State state = getState();
        Log.d(TAG, "run: ====>>"+state);
    }

    @Override
    public synchronized void start() {
        super.start();
    }
}
