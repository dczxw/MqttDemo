package com.sm.supere.imps;

public interface OnClientListener {

    void send(String topic, String content);
    void receive(String topic, String content);
    void subscribe(String topic);
    void unSubscribe(String topic);
}
