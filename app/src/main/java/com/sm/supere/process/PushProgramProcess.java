package com.sm.supere.process;

import com.sm.supere.base.BaseProcess;
import com.sm.supere.constant.MqClientManager;

public class PushProgramProcess extends BaseProcess {

    public static final String cmd = "pushProgram";

    @Override
    public String getCMD() {
        return cmd;
    }

    @Override
    public void send(String topic, String content) {
        MqClientManager.getInstance().send(topic, content);
    }

    @Override
    public void receive(String topic, String content) {

    }
}