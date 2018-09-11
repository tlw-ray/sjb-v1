package com.xskr.common;

public class XskrMessage {

    private String message;
    private Object entity;

    public XskrMessage() {
    }

    public XskrMessage(String message, Object entity) {
        this.message = message;
        this.entity = entity;
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


}
