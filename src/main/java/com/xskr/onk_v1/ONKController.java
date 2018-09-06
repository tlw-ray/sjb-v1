package com.xskr.onk_v1;

import com.alibaba.fastjson.JSONArray;
import com.xskr.onk_v1.core.Card;
import com.xskr.onk_v1.core.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/onk")
public class ONKController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static int zoomID_Generator = 0;
    private Map<Integer, Room> idZoomMap = new ConcurrentHashMap();

    @RequestMapping(path = "/echo", method = RequestMethod.POST)
    public String echo(@RequestParam String msg){
        return msg;
    }

    /**
     * 创建房间，需要传入该房间支持的角色列表
     * @param cardNames 角色名称清单
     * @return 房间号码
     */
    @RequestMapping(path = "/zoom", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public int createRoom(@RequestBody String[] cardNames){
        logger.debug(Arrays.toString(cardNames));
        List<Card> cards = new ArrayList();
        for(String cardName:cardNames){
            Card card = Card.valueOf(cardName);
            cards.add(card);
        }
        Room room = new Room(cards);
        zoomID_Generator++;
        idZoomMap.put(zoomID_Generator, room);
        return zoomID_Generator;
    }

    //玩家进入房间
    @RequestMapping("/{roomID}/join")
    public void login(@PathVariable int roomID, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.join(userName);
    }

    //玩家离开
    @RequestMapping("/{roomID}/leave")
    public void leave(@PathVariable int roomID, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.leave(userName);
    }

    //玩家准备
    @RequestMapping("/{roomID}/ready/{seat}")
    public void ready(@PathVariable int roomID, @PathVariable int seat, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.ready(userName, seat);
    }

    //捣蛋鬼换牌
    @RequestMapping("/{roomID}/troublemaker/exchange/{seat1}/{seat2}")
    public void exchangeCard(@PathVariable int roomID, @PathVariable int seat1, @PathVariable int seat2, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.troublemakerExchangeCard(userName, seat1, seat2);
    }

    //强盗换牌
    @RequestMapping("/{roomID}/robber/snatch/{seat}")
    public void snatchCard(@PathVariable int roomID, @PathVariable int seat, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.robberSnatchCard(userName, seat);
    }

    //狼人验牌
    @RequestMapping("/{roomID}/singleWolf/check/{deck}")
    public void wolfCheckDeck(@PathVariable int roomID, @PathVariable int deck, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.singleWolfCheckDeck(userName, deck);
    }

    //预言家验牌
    @RequestMapping("/{roomID}/seer/check/{deck1}/{deck2}")
    public void seerCheckDeck(@PathVariable int roomID, @PathVariable int deck1, @PathVariable int deck2, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.seerCheckDeck(userName, deck1, deck2);
    }

    //预言家验人
    @RequestMapping("/{roomID}/seer/check/{seat}")
    public void seerCheckPlayer(@PathVariable int roomID, @PathVariable int seat, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.seerCheckPlayer(userName, seat);
    }

    //酒鬼换牌
    @RequestMapping("/{roomID}/drunk/exchange/{deck}")
    public void drunkExchangeCard(@PathVariable int roomID, @PathVariable int deck, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.drunkExchangeCard(userName, deck);
    }

    //投票
    @RequestMapping("/{roomID}/vote/{seat}")
    public void vote(@PathVariable int roomID, @PathVariable int seat, HttpSession session){
        String userName = (String)session.getAttribute("username");
        Room room = idZoomMap.get(roomID);
        room.vote(userName, seat);
    }
}
