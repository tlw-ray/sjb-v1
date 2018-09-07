package com.xskr.onk_v1;

import com.xskr.onk_v1.core.Card;
import com.xskr.onk_v1.core.Player;
import com.xskr.onk_v1.core.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/onk")
public class ONKController{

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static int RoomID_Generator = 0;
    private Map<Integer, Room> idRoomMap = Collections.synchronizedMap(new TreeMap());

    /**
     * 创建房间，需要传入该房间支持的角色列表
     * @param cardNames 角色名称清单
     * @return 房间号码
     */
    @RequestMapping(path = "/room", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public int createRoom(@RequestBody String[] cardNames){
        logger.debug(Arrays.toString(cardNames));
        List<Card> cards = new ArrayList();
        for(String cardName:cardNames){
            Card card = Card.valueOf(cardName);
            cards.add(card);
        }
        Room room = new Room(cards);
        RoomID_Generator++;
        idRoomMap.put(RoomID_Generator, room);
        return RoomID_Generator;
    }

    @RequestMapping(path = "/room/{roomID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Room getRoom(@PathVariable int roomID){
        return idRoomMap.get(roomID);
    }

    /**
     * 列举现有的房间
     * @return
     */
    @RequestMapping(path="/rooms")
    public Set<Integer> listRoom(){
        return idRoomMap.keySet();
    }

    /**
     * 列举房间内现有的玩家信息
     * @param roomID
     * @return
     */
    @RequestMapping(path="/{roomID}/players")
    public Set<Player> listPlayer(@PathVariable int roomID){
        Room room = idRoomMap.get(roomID);
        return room.getPlayers();
    }

    /**
     * 玩家进入房间, 系统给他分配一个座位
     * @param roomID
     * @return
     */
    @RequestMapping("/{roomID}/join")
    public int join(@PathVariable int roomID){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        if(room != null) {
            return room.join(userName);
        }else{
            return -1;
        }
    }

    /**
     * 玩家离开
     */
    @RequestMapping("/{roomID}/leave")
    public void leave(@PathVariable int roomID){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.leave(userName);
    }

    /**
     * 玩家选择座位
     * @param roomID
     * @param seat
     */
    @RequestMapping("/{roomID}/sit/{seat}")
    public void sit(@PathVariable int roomID, @PathVariable int seat){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.sit(userName, seat);
    }

    /**
     * 玩家设定准备
     */
    @RequestMapping("/{roomID}/ready/{ready}")
    public void ready(@PathVariable int roomID, @PathVariable boolean ready){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.setReady(userName, ready);
    }

    /**
     * 开启新的游戏
     * @param roomID
     */
    @RequestMapping("/{roomID}/new")
    public void newGame(@PathVariable int roomID){
        Room room = idRoomMap.get(roomID);
        room.newGame();
    }

    /**
     * 捣蛋鬼换牌
     * @param roomID
     * @param seat1
     * @param seat2
     */
    @RequestMapping("/{roomID}/troublemaker/exchange/{seat1}/{seat2}")
    public void exchangeCard(@PathVariable int roomID, @PathVariable int seat1, @PathVariable int seat2){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.troublemakerExchangeCard(userName, seat1, seat2);
    }

    /**
     * 强盗换牌
     * @param roomID
     * @param seat
     */
    @RequestMapping("/{roomID}/robber/snatch/{seat}")
    public void snatchCard(@PathVariable int roomID, @PathVariable int seat){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.robberSnatchCard(userName, seat);
    }

    /**
     * 狼人验牌
     * @param roomID
     * @param deck
     */
    @RequestMapping("/{roomID}/singleWolf/check/{deck}")
    public void wolfCheckDeck(@PathVariable int roomID, @PathVariable int deck){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.singleWolfCheckDeck(userName, deck);
    }

    /**
     * 预言家验牌
     * @param roomID
     * @param deck1
     * @param deck2
     */
    @RequestMapping("/{roomID}/seer/check/{deck1}/{deck2}")
    public void seerCheckDeck(@PathVariable int roomID, @PathVariable int deck1, @PathVariable int deck2){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.seerCheckDeck(userName, deck1, deck2);
    }

    /**
     * 预言家验人
     * @param roomID
     * @param seat
     */
    @RequestMapping("/{roomID}/seer/check/{seat}")
    public void seerCheckPlayer(@PathVariable int roomID, @PathVariable int seat){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.seerCheckPlayer(userName, seat);
    }

    /**
     * 酒鬼换牌
     * @param roomID
     * @param deck
     */
    @RequestMapping("/{roomID}/drunk/exchange/{deck}")
    public void drunkExchangeCard(@PathVariable int roomID, @PathVariable int deck){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.drunkExchangeCard(userName, deck);
    }

    /**
     * 投票
     * @param roomID
     * @param seat
     */
    @RequestMapping("/{roomID}/vote/{seat}")
    public void vote(@PathVariable int roomID, @PathVariable int seat){
        String userName = getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.vote(userName, seat);
    }

    private String getCurrentUserName(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
            .getAuthentication()
            .getPrincipal();
        return userDetails.getUsername();
    }
}