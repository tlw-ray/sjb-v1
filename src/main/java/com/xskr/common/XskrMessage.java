package com.xskr.common;

import com.xskr.onk_v1.core.ClientAction;

public class XskrMessage {
    //要传递的消息
    private String message;
    //要执行的指令
    private ClientAction action;
    //要传递的数据
    private Object entity;

    public XskrMessage() {
    }

    public XskrMessage(String message, ClientAction action, Object entity) {
        this.message = message;
        this.action = action;
        this.entity = entity;
    }

    public ClientAction getAction() {
        return action;
    }

    public void setAction(ClientAction action) {
        this.action = action;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getMessage() {
        return message;
    }

    public Object getEntity() {
        return entity;
    }

    @Override
    public String toString() {
        return "XskrMessage{" +
                "message='" + message + '\'' +
                ", action='" + action + '\'' +
                ", entity=" + entity +
                '}';
    }
}
