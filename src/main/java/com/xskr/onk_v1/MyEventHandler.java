package com.xskr.onk_v1;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MyEventHandler {
    @EventListener
    public void event(Object obj){
        System.out.println(obj);
    }
}
