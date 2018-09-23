package com.xskr.onk_v1;

import com.xskr.common.WebUtil;
import com.xskr.onk_v1.core.Card;
import com.xskr.onk_v1.core.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/onk")
public class ONKController{

    private static int RoomID_Generator = 0;

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<Integer, Room> idRoomMap = Collections.synchronizedMap(new TreeMap());

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @RequestMapping("/cards")
    public Card[] getCards(){
        return Card.values();
    }

    /**
     * 创建房间，需要传入该房间支持的角色列表
     * @return 房间静态信息, ID, 现有玩家清单, 座位数量等, 角色列表
     */
    @RequestMapping(path = "/room", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
    @RequestMapping("/{roomID}/join")
    public void join(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.join(userName);
    }

    @RequestMapping("/{roomID}/exit")
    public void exit(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.leave(userName);
    }

    //TODO 注意: 所有返回Room的方法均导致玩家手牌信息的泄露
    @RequestMapping(path = "/room/{roomID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Room getRoom(@PathVariable int roomID){
        Room room = idRoomMap.get(roomID);
        return room;
    }

//    /**
//     * 列举现有的房间
//     * @return
//     */
//    @RequestMapping(path="/rooms")
//    public Set<Integer> listRoom(){
//        return idRoomMap.keySet();
//    }

//    /**
//     * 列举房间内现有的玩家信息
//     * @param roomID
//     * @return
//     */
//    @RequestMapping(path="/{roomID}/players")
//    public Set<Player> listPlayer(@PathVariable int roomID){
//        Room room = idRoomMap.get(roomID);
//        return room.getPlayers();
//    }

    /**
     * 玩家离开
     */
    @RequestMapping("/{roomID}/leave")
    public void leave(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.leave(userName);
    }

//    /**
//     * 玩家选择座位
//     * @param roomID
//     * @param seat
//     */
//    @RequestMapping("/{roomID}/sit/{seat}")
//    public void sit(@PathVariable int roomID, @PathVariable int seat){
//        String userName = getCurrentUserName();
//        Room room = idRoomMap.get(roomID);
//        room.sit(userName, seat);
//    }

    /**
     * 玩家设定准备, 如果所有玩家均已准备则触发游戏开始
     * @param roomID 房间ID
     * @param ready 是否准备
     * @return 服务端返回的该玩家准备状态
     */
    @RequestMapping("/{roomID}/ready/{ready}")
    public boolean ready(@PathVariable int roomID, @PathVariable boolean ready){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        return room.setReady(userName, ready);
    }

    @RequestMapping("/{roomID}/keyMessages")
    public List<String> getKeyMessages(@PathVariable int roomID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        return room.getKeyMessages(userName);
    }

    /**
     * 玩家点击了桌上的一张牌
     * @return
     */
    @RequestMapping("/{roomID}/deck/{cardID}")
    public void pickCard(@PathVariable int roomID, @PathVariable int cardID){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.pickCard(userName, cardID);
    }

    @RequestMapping("/{roomID}/seat/{seat}")
    public void pickSeat(@PathVariable int roomID, @PathVariable int seat){
        String userName = WebUtil.getCurrentUserName();
        Room room = idRoomMap.get(roomID);
        room.pickSeat(userName, seat);
    }

    @RequestMapping(path = "/{roomID}/cards", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
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
