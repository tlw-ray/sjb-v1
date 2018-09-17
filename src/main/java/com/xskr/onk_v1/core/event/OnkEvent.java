package com.xskr.onk_v1.core.event;

import java.util.Date;
import java.util.List;

public class OnkEvent {

    Date date = new Date();
    Subject subject;
    Operation operation;
    List<Subject> objects;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public List<Subject> getObjects() {
        return objects;
    }

    public void setObjects(List<Subject> objects) {
        this.objects = objects;
    }
}
