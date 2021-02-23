package com.sm.supere.constant;

import com.sm.supere.base.BaseProcess;
import com.sm.supere.imps.OnClientListener;
import com.sm.supere.process.CommonProcess;
import com.sm.supere.process.LoginProcess;
import com.sm.supere.process.PushProgramProcess;
import com.sm.supere.services.MQTTService;

import java.util.HashMap;
import java.util.Map;

/**
 * 控制层
 */
public class MqClientManager implements OnClientListener {

    public static MqClientManager INSTANCE = null;
    private Map<String, BaseProcess> processList = new HashMap<>();

    public static MqClientManager getInstance() {
        synchronized (MqClientManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new MqClientManager();
            }
        }
        return INSTANCE;
    }

    public MqClientManager() {
        // 注册所有的mqtt监听事件
        registerProcess(new LoginProcess());
        registerProcess(new PushProgramProcess());
        registerProcess(new CommonProcess());
    }

    public void registerProcess(BaseProcess process) {
        this.processList.put(process.getCMD(), process);
    }

    @Override
    public void send(String topic, String content) {
        send(topic, content, true);
    }


    public void send(String topic, String content, boolean isFormat) {
        if (MQTTService.isConnected()) {
            if (isFormat) {
                MQTTService.publish(topic, content);
            } else {
                MQTTService.publishCommon(topic, content);
            }
        }
    }

    @Override
    public void receive(String topic, String content) {
        try {
            BaseProcess process = processList.get(topic);
            if (process == null) { // 如果找不到订阅的信息，做通用事件处理
                process = new CommonProcess();
            }
            process.receive(topic, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String topic) {
        if (MQTTService.isConnected()) {
            MQTTService.subscribe(topic);
        }
    }

    @Override
    public void unSubscribe(String topic) {
        if (MQTTService.isConnected()) {
            MQTTService.unSubscribe(topic);
        }
    }
}
