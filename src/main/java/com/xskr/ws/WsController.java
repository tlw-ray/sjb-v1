package com.xskr.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;

import java.security.Principal;

/**
 * Created by 唐力伟 on 2017/5/12 22:09.
 */
@Controller
@EnableScheduling
public class WsController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleChat(Principal principal, TextMessage msg){
        System.out.println(msg.getClass());
        System.out.println(msg);
        if(principal.getName().equals("tlw")){
            messagingTemplate.convertAndSendToUser("tlw", "/queue/notifications", principal.getName() + "-send:" + msg);
        }else{
            messagingTemplate.convertAndSendToUser("dss", "/queue/notifications", principal.getName() + "-send:" + msg);
        }
    }

    @Scheduled(fixedRate = 2000)
    public void stat() {
//        simpMessagingTemplate.convertAndSend("/topic", "Public Message");//可以发送并接收成功
        messagingTemplate.convertAndSendToUser("dss", "/queue/notifications", "Private Message ...");
        System.out.println("Send...");
    }
}
