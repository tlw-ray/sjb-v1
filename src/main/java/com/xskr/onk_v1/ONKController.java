package com.xskr.onk_v1;

import com.alibaba.fastjson.JSONArray;
import com.xskr.onk_v1.core.Card;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ONKController {

    //接收房间定义的玩家角色参数
    @RequestMapping(path = "/room", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String createRoom(@RequestParam String characters){
        JSONArray jsonArray = (JSONArray) JSONArray.parse(characters);
        String[] characterArray = jsonArray.toArray(new String[0]);
        List<Card> cards = new ArrayList();
        for(String characterName : characterArray){
            Card card = Card.valueOf(characterName);
            cards.add(card);
        }
        return "success";
    }

    //玩家进入房间
    public void login(){

    }


}
