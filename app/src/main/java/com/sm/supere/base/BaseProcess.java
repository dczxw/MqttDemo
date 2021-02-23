package com.sm.supere.base;

public abstract class BaseProcess {
    protected static String cmd;

    public abstract String getCMD();

    public abstract void send(String topic, String content);

    public abstract void receive(String topic, String content);
}
