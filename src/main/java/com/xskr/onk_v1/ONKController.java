package com.xskr.onk_v1;

import com.xskr.common.WebUtil;
import com.xskr.onk_v1.core.Card;
import com.xskr.onk_v1.core.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/onk")
public class ONKController{

    private static int RoomID_Generator = 0;

    private Map<Integer, Room> idRoomMap = Collections.synchronizedMap(new TreeMap());

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * 获得当前一夜狼游戏的所有可用卡牌
     * @return
     */
    @RequestMapping(path = "/cards", produces = MediaType.APPLICATION_JSON_VALUE)
    public Card[] getCards(){
        return Card.values();
    }

    /**
     * 创建房间，需要传入该房间支持的角色列表
     * @return 房间静态信息, ID, 现有玩家清单, 座位数量等, 角色列表
     */
    @RequestMapping(path = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public int createRoom(){
        int roomID = RoomID_Generator++;
        Room room = new Room(roomID, WebUtil.getCurrentUserName());
        room.setSimpMessagingTemplate(simpMessagingTemplate);
        idRoomMap.put(roomID, room);
        return roomID;
    }

    /**
     * 玩家进入房间, 返回当前房间的座位到玩家名称的映射关系
     * @param roomID
     * @return
     */
    @RequestMapping(path = "/join/{roomID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void pickJoin(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.join(userName);
    }

    /**
     * 玩家离开
     */
    @RequestMapping(path = "/leave/{roomID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void pickLeave(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.leave(userName);
    }

    /**
     * 玩家设定准备, 如果所有玩家均已准备则触发游戏开始
     * @param roomID 房间ID
     * @param ready 是否准备
     * @return 服务端返回的该玩家准备状态
     */
    @RequestMapping(path = "/ready/{roomID}/{ready}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean pickReady(@PathVariable int roomID, @PathVariable boolean ready){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        return room.setReady(userName, ready);
    }

    /**
     * 获得某个座位已有的关键信息
     * @param roomID
     * @return
     */
    @RequestMapping(path = "/getKeyMessages/{roomID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getKeyMessages(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        return room.getKeyMessages(userName);
    }

    /**
     * 玩家点击了桌上的一张牌
     * @return
     */
    @RequestMapping(path = "/card/{roomID}/{cardID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void pickCard(@PathVariable int roomID, @PathVariable int cardID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.pickDesktopCard(userName, cardID);
    }

    /**
     * 玩家点了某个座位
     * @param roomID
     * @param seat
     */
    @RequestMapping(path = "/seat/{roomID}/{seat}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void pickSeat(@PathVariable int roomID, @PathVariable int seat){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.pickSeat(userName, seat);
    }

    /**
     * 获得当前房间设定的牌
     * @param roomID
     * @param cards
     */
    @RequestMapping(path = "/cards/{roomID}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public void setCards(@PathVariable int roomID,@RequestBody Card[] cards){
        String userName = WebUtil.getCurrentUserName();
        System.out.println(userName + ": set cards ...................." + Arrays.toString(cards));
        System.out.println(roomID);
        Room room = idRoomMap.get(roomID);
        System.out.println(room);
        System.out.println(room.getOwner());
        if(userName.equals(room.getOwner())){
            room.setCards(cards);
        }
    }
}
