package com.sm.supere.process;

import android.util.Log;

import com.sm.supere.base.BaseProcess;
import com.sm.supere.model.HandlerMessage;
import com.sm.supere.utils.HandlerManager;

public class LoginProcess extends BaseProcess {

    public static final String cmd = "login";

    @Override
    public String getCMD() {
        return cmd;
    }

    @Override
    public void send(String topic, String content) {

    }

    @Override
    public void receive(String topic, String content) {
        Log.d(cmd, "receive: ===>>" + content);
        HandlerManager.getInstance().send(new HandlerMessage(topic, content));
    }


}
