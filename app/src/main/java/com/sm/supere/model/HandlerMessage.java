package com.sm.supere.model;

public class HandlerMessage {
    private String topic;
    private String content;

    public HandlerMessage(String topic, String content) {
        this.topic = topic;
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }
}