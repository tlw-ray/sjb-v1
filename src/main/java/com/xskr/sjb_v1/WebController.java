package com.xskr.sjb_v1;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.Principal;

@Controller
public class WebController {

    Engine engine = new Engine();
    {
        engine.setEnter("<BR>");
    }

    @SendTo("/topic")
    @MessageMapping("/req")
    public String handle(Principal principal, String message){
        String line = principal.getName()+":"+message;
        System.out.println(principal.getName()+":"+message);
        try {
            String result = engine.play(line);
            if(result != null){
                return result;
            }else{
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
