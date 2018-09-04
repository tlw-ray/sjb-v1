package com.xskr.common;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyEventHandler {
    @EventListener
    public void event(Object obj){
        System.out.println(obj);
    }
}
