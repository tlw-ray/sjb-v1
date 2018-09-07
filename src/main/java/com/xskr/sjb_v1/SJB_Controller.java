package com.xskr.sjb_v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xskr.sjb_v1.core.Engine;
import com.xskr.sjb_v1.model.DataPack;
import com.xskr.sjb_v1.model.Finger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class SJB_Controller {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ObjectMapper objectMapper = new ObjectMapper();

    private Engine engine = new Engine();
    {
        engine.setEnter("<BR>");
    }

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/req")
    public void handle(Principal principal, String command){
        String playerName = principal.getName();
        Finger finger = Finger.valueOf(command);
        logger.debug(playerName + ":" + command);
        engine.action(playerName, finger);
    }

    @EventListener
    public void onLogin(SimpMessagingTemplate simpMessagingTemplate){
        System.out.println("------------00000");
        System.out.println(simpMessagingTemplate);
    }


    @Scheduled(fixedRate = 5000)
    public void stat() throws JsonProcessingException {
        // 如果计算出结果则发回并清除猜拳状态，进入下一个回合
        Map<String, DataPack> playerDataPack = engine.calc();
        if(playerDataPack != null) {
            String message = objectMapper.writeValueAsString(playerDataPack);
            logger.debug(message);
            simpMessagingTemplate.convertAndSend("/topic", playerDataPack);
            engine.clearFinger();
        }
    }
}
