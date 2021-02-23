package com.sm.supere.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.sm.supere.imps.OnHandlerListener;


public class HandlerManager {

    public static HandlerManager instance = null;
    private Handler mHandler = null;
    private OnHandlerListener listener;


    @SuppressLint("HandlerLeak")
    public HandlerManager() {
        this.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                listener.handlerMessage(msg.obj);
            }
        };
    }

    public static HandlerManager getInstance() {
        synchronized (HandlerManager.class) {
            if (instance == null) {
                instance = new HandlerManager();
            }
        }
        return instance;
    }


    public void send(Object o) {
        Message msg = new Message();
        msg.obj = o;
        mHandler.sendMessage(msg);
    }

    public void setListener(OnHandlerListener listener) {
        this.listener = listener;
    }
}
