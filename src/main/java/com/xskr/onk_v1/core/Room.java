package com.xskr.onk_v1.core;

import com.alibaba.fastjson.JSON;
import com.xskr.common.XskrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

public class Room {

    private Logger logger = LoggerFactory.getLogger(getClass());

    static final int TABLE_DECK_THICKNESS = 3;
    private static final int MAX_SEAT = 12;

    //房间号
    private int id;
    // 该房间支持的所有卡牌
    private Card[] cards;
    // 房间内的座位
    private List<Seat> seats = new ArrayList();
    //进入房间还但没有座位的玩家
    private Set<String> observers = new TreeSet();
    // 发牌后剩余的桌面3张牌垛
    private TreeMap<Integer, Card> desktopCards = new TreeMap();
    // 房主用户名
    private String owner;


    // 玩家夜间操作
    private Integer singleWolfCheckDesktopCard;
    private Integer seerCheckDesktopCard1;
    private Integer seerCheckDesktopCard2;
    private Integer seerCheckPlayerSeat;
    private Integer robberSnatchPlayerSeat;
    private Integer troublemakerExchangePlayerSeat1;
    private Integer troublemakerExchangePlayerSeat2;
    private Integer drunkExchangeDesktopCard;
    private boolean hunterVote = false;

    //游戏是否处于开始状态，如果是就不能再进行准备切换
    private Scene scene = Scene.PREPARE;

    //用于发送WebSocket信息
    private SimpMessagingTemplate simpMessagingTemplate;

    public Room(int id, String owner){
        logger.debug("Room(id={}, owner={})", id, owner);
        this.id = id;
        this.owner = owner;
        this.cards = new Card[]{
            Card.WEREWOLF_1, Card.MINION, Card.SEER, Card.ROBBER, Card.INSOMNIAC
        };
        for(int i=0;i<MAX_SEAT;i++){
            Seat seat = new Seat();
            seats.add(seat);
        }
    }

    /**
     * 获得可用的座位数
     * @return
     */
    public int getAvailableSeatCount(){
        if(cards == null){
            return -1;
        }else{
            return cards.length - TABLE_DECK_THICKNESS;
        }
    }

    /**
     * 玩家进入房间
     * @param userName
     * @return 返回房间信息，用来区分断线重连
     */
    public void join(String userName){
        logger.debug("join(userName = {})", userName);
        Seat playerSeat = getSeatByUserName(userName);
        //游戏在不同状态有人加入
        if(scene == Scene.PREPARE){
            //加入准备状态的房间,默认行为是找一个空座位坐下
            boolean seated = false;
            for(int i=0;i<getAvailableSeatCount();i++){
                Seat seat = getSeats().get(i);
                if(seat.getUserName() == null){
                    seat.setUserName(userName);
                    seated = true;
                    break;
                }
            }
            //如果没有空座位了就加入observer
            if(!seated){
                observers.add(userName);
            }else{
                //do nothing
            }
            //告知大家新玩家进入和坐下，座位状态变化了
            XskrMessage roomChangedMessage = new XskrMessage(null, ClientAction.ROOM_CHANGED, this);
            sendMessage(roomChangedMessage);
        }else{
            //游戏在进行中有人加入
            Seat oldUserSeat = getSeatByOldUserName(userName);
            if(oldUserSeat != null){
                //玩家之前从座位上离开了，现在回到了座位，断线重连
                oldUserSeat.setUserName(userName);
                //告知所有玩家xxx回来了
                String message = String.format("%s回来了", userName);
                XskrMessage xskrMessage = new XskrMessage(message, ClientAction.ROOM_CHANGED, this);
                sendMessage(xskrMessage);
                //告知自己接下来该做的操作
                XskrMessage reconnectXskrMessage = new XskrMessage(null, ClientAction.RECONNECT, this);
                sendMessage(userName, reconnectXskrMessage);
            }else{
                //加入observer
                observers.add(userName);
                XskrMessage xskrMessage = new XskrMessage(null, ClientAction.ROOM_CHANGED, this);
                sendMessage(userName, xskrMessage);
            }
        }
    }

    /**
     * 玩家离开房间
     * 普通游戏进行时应该不允许玩家离开房间，但桌游可能有玩家离开后由observer加入到游戏中来接替他
     * @param userName
     */
    public void leave(String userName){
        logger.debug("leave(userName = {})", userName);
        if(observers.remove(userName)){
            //从观看者中移除
        }else{
            //从座位上移除该玩家
            Seat playerSeat = getSeatByUserName(userName);
            playerSeat.setUserName(null);
            String message = String.format("%s离开房间", userName);
            XskrMessage roomChangedMessage = new XskrMessage(message, ClientAction.ROOM_CHANGED, this);
            sendMessage(roomChangedMessage);
        }
        XskrMessage roomChangedMessage = new XskrMessage(null, ClientAction.LEAVE_ROOM, this);
        sendMessage(userName, roomChangedMessage);
    }

    /**
     * 玩家坐到指定位置, 当所有玩家都准备好时游戏自动开始
     * TODO 或许应该加个倒数5,4,3,2,1
     * @param userName 玩家
     * @return 该玩家最终处于的ready状态
     */
    public void setReady(String userName, boolean ready){
        if(scene == Scene.ACTIVATE || scene == Scene.VOTE) {
            //如果已经开始或正在投票则不可改变ready状态
        }else if(scene == Scene.PREPARE){
            logger.debug("setReady(userName = {}, pickReady = {})", userName, ready);
            Seat seat = getSeatByUserName(userName);

            if(seat != null) {
                //玩家准备状态改变
                seat.setReady(ready);
                XskrMessage xskrMessage = new XskrMessage("", ClientAction.ROOM_CHANGED, this);
                sendMessage(xskrMessage);
                if(ready) {
                    //检查是否能够触发游戏开始事件
                    //如果玩家数量达到座位数量，且玩家都是ready状态则触发新游戏事件
                    boolean allReady = true;
                    for (int i = 0; i < getAvailableSeatCount(); i++) {
                        Seat anySeat = seats.get(i);
                        if (!anySeat.isReady() || anySeat.getUserName() == null) {
                            allReady = false;
                            break;
                        }
                    }
                    if (allReady) {
                        newGame();
                    } else {
                        //人数未达到座位数游戏无法开始
                    }
                }else{
                    //如果玩家取消准备那么不需要检查游戏是否开始
                }
            }else{
                if(observers.contains(userName)){
                    String message = String.format("玩家%s是观看者，无法设定准备状态。", userName);
                    throw new RuntimeException(message);
                }else{
                    String message = String.format("玩家%s不在该房间，无法设定准备状态。", userName);
                    throw new RuntimeException(message);
                }
            }
        }else{
            //TODO 客户端应根据场景控制Ready按钮可用性，禁止用户发出此请求
            throw new RuntimeException("未支持的场景: " + scene);
        }
    }

    /**
     * 根据初始卡牌查询用户
     * @param card
     * @return
     */
    public Seat getSeatByInitializeCard(Card card){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getInitializeCard() == card){
                return playerSeat;
            }
        }
        return null;
    }

    //TODO 这里可以写为函数式，在getPlayer方法中增加一个过滤条件
    public Seat getSeatByUserName(String userName){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getUserName()!=null && playerSeat.getUserName().equals(userName)){
                return playerSeat;
            }
        }
        return null;
    }
    public Seat getSeatByOldUserName(String oldUserName){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat oldUserSeta = seats.get(i);
            if(oldUserSeta.getUserName()==null && oldUserSeta.getOldUserName().equals(oldUserName)){
                return oldUserSeta;
            }
        }
        return null;
    }

    /**
     * 根据卡牌查询用户，卡牌是夜晚行动过后被交换过的
     * @param card
     * @return
     */
    public Seat getPlayerSeatByCard(Card card){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getCard() == card){
                return playerSeat;
            }
        }
        return null;
    }

    /**
     * 初始化一局游戏
     * @return 如果开始新一局游戏返回true， 若未能开始返回false
     */
    private void newGame(){
        //如果有座位空着或者座位上的玩家不在ready状态则无法开始新游戏
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getUserName() == null){
                String message = String.format("%s号座位没有玩家，游戏无法开始。", i);
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(xskrMessage);
                return;
            }else if(!playerSeat.isReady()){
                String message = String.format("%s号座位玩家未进入准备状态，游戏无法开始。", i);
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(xskrMessage);
                return;
            }else{
                //do nothing
            }
        }

        //标记进入游戏状态
        scene = Scene.ACTIVATE;
        logger.debug("newGame()");
        XskrMessage xskrMessage = new XskrMessage("新一局开始了！", ClientAction.NEW_GAME, null);
        sendMessage(xskrMessage);

        Deck deck = new Deck(cards);

        //清空上一局所有角色的操作状态
        singleWolfCheckDesktopCard = null;
        seerCheckDesktopCard1 = null;
        seerCheckDesktopCard2 = null;
        seerCheckPlayerSeat = null;
        robberSnatchPlayerSeat = null;
        troublemakerExchangePlayerSeat1 = null;
        troublemakerExchangePlayerSeat2 = null;
        drunkExchangeDesktopCard = null;
        //清空上一局所有玩家的身份与投票状态
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            playerSeat.setCard(null);
        }

        //洗牌
        deck.shuffle(500);

        //为所有人发牌，清空玩家状态
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            Card card = deck.deal();
            playerSeat.setCard(card);
            playerSeat.setInitializeCard(card);
            playerSeat.setVoteSeat(null);
        }

        //建立桌面剩余的牌垛
        desktopCards.put(0, deck.deal());
        desktopCards.put(1, deck.deal());
        desktopCards.put(2, deck.deal());

        logger.debug("playerAction");
        //需要主动行动的玩家
        Seat singleWolfSeat = getSingleWolfSeat();
        Seat seerSeat = getSeatByInitializeCard(Card.SEER);
        Seat robberSeat = getSeatByInitializeCard(Card.ROBBER);
        Seat troublemakerSeat = getSeatByInitializeCard(Card.TROUBLEMAKER);
        Seat drunkSeat = getSeatByInitializeCard(Card.DRUNK);

        //发牌结束后根据身份为每个玩家发送行动提示信息
        Map<Seat, XskrMessage> playerXskrMessageMap = new HashMap();
        boolean directVote = true;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            String message;
            ClientAction clientAction = null;
            if(playerSeat == singleWolfSeat){
                message = String.format("请点选牌1、牌2、牌3中的一张，来查看桌面牌垛中对应的卡牌。");
                clientAction = ClientAction.SINGLE_WOLF_ACTION;
                directVote = false;
            }else if (playerSeat == seerSeat) {
                message = "请点选牌1、牌2、牌3中的任意两个来查看的桌面牌垛中对应的卡牌，或者点选一位玩家查看其身份。";
                clientAction = ClientAction.SEER_ACTION;
                directVote = false;
            }else if(playerSeat == robberSeat){
                message = "请输点选任意玩家，查阅其卡牌并交换身份。";
                clientAction = ClientAction.ROBBER_ACTION;
                directVote = false;
            }else if(playerSeat == troublemakerSeat){
                message = "请点选除您之外两个玩家，交换他们的身份。";
                clientAction = ClientAction.TROUBLEMAKER_ACTION;
                directVote = false;
            }else if(playerSeat == drunkSeat){
                message = "请点选牌1、牌2、牌3中任意一张，与之交换身份。";
                clientAction = ClientAction.DRUNK_ACTION;
                directVote = false;
            }else{
                message = "所有玩家行动完成后，系统会给出下一步的信息。";
            }
            //预备玩家身份和操作信息
            XskrMessage xskrMessage1 = new XskrMessage(String.format("您的初始身份是%s。<br>" + message, getDisplayName(playerSeat.getCard())), clientAction, null);
            playerXskrMessageMap.put(playerSeat, xskrMessage1);
        }
        //向玩家发送身份和操作提示信息
        for(Map.Entry<Seat, XskrMessage> entry:playerXskrMessageMap.entrySet()){
            Seat seat = entry.getKey();
            XskrMessage xskrMessage1 = entry.getValue();
            sendMessage(seat.getUserName(), xskrMessage1);
            keepKeyMessage(seat, xskrMessage1);
        }
        if(directVote){
            //没有任何玩家需要行动，直接进入投票阶段
            Random random = new Random();
            //随机等待约10秒，至少3秒，模拟有人在行动的情况
            long span = 3000 + random.nextInt(3000);
            try {
                Thread.sleep(span);
            }catch(Exception e){
                e.printStackTrace();
            }
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    attemptNightAction();
                }
            };
            //这里等待后触发，多线程是否有问题
            //TODO 无论是否可以直接进入投票阶段，都应至少等待10秒钟
            Timer timer = new Timer();
            timer.schedule(timerTask, 10000);
        }else{
            // do nothing
        }
    }

    public TreeMap<Integer, Card> getDesktopCards() {
        return desktopCards;
    }

    //是否能够进入白天，当所有需要操作的玩家行动完毕才能进入白天
    private boolean canNightAction(){
        logger.debug("canNightAction()");

        // 检查孤狼是否已经行动
        if(getSingleWolfSeat() != null){
            //如果存在孤狼的角色
            if(singleWolfCheckDesktopCard == null){
                //如果孤狼没有指定要看的牌垛中的一张牌
                logger.debug("singleWolf not work!");
                return false;
            }
        }

        //检查预言家是否已经行动
        if(getSeatByInitializeCard(Card.SEER) != null){
            //如果存在预言家角色
            if((seerCheckDesktopCard1 == null || seerCheckDesktopCard2 == null) &&
                    seerCheckPlayerSeat == null){
                //如果语言家既没有指定要看牌垛中的那两张牌，也没有指定要验证的玩家的身份
                logger.debug("seer not work!");
                return false;
            }
        }

        // 检查强盗是否已经行动
        if(getSeatByInitializeCard(Card.ROBBER) != null){
            //如果存在强盗玩家
            if(robberSnatchPlayerSeat == null){
                // 如果强盗没有指定要交换的位置
                logger.debug("robber not work!");
                return false;
            }
        }

        //检查捣蛋鬼是否已经行动
        if(getSeatByInitializeCard(Card.TROUBLEMAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troublemakerExchangePlayerSeat1 == null
                    || troublemakerExchangePlayerSeat2 == null){
                //如果捣蛋鬼没有指定要交换的位置
                logger.debug("troublemaker not work!");
                return false;
            }
        }

        //检查酒鬼是否行动
        if(getSeatByInitializeCard(Card.DRUNK) != null){
            if(drunkExchangeDesktopCard == null){
                logger.debug("drunk not work!");
                return false;
            }
        }

        //通过了所有的检查
        return true;
    }

    //获得孤狼玩家
    protected Seat getSingleWolfSeat(){
        Seat wolf1 = getSeatByInitializeCard(Card.WEREWOLF_1);
        Seat wolf2 = getSeatByInitializeCard(Card.WEREWOLF_2);
        if(wolf1 == null && wolf2 != null){
            return wolf2;
        }else if(wolf1 != null && wolf2 == null){
            return wolf1;
        }else{
            return null;
        }
    }

    //夜间行动: 如果所有玩家均已声明行动完毕，开始真正处理玩家们的行动，处理所有流程并发布最新的信息，否则什么也不做
    public void attemptNightAction(){
        logger.debug("attemptNightAction()");
        if(canNightAction()){
//          Seat doopelganger = initializeCardPlayerMap.get(Card.DOPPELGANGER);
            Seat singleWolfSeat = getSingleWolfSeat();
            Seat wolf1Seat = getSeatByInitializeCard(Card.WEREWOLF_1);
            Seat wolf2Seat = getSeatByInitializeCard(Card.WEREWOLF_2);
            Seat minionSeat = getSeatByInitializeCard(Card.MINION);
            Seat meson1Seat = getSeatByInitializeCard(Card.MASON_1);
            Seat meson2Seat = getSeatByInitializeCard(Card.MASON_2);
            Seat seerSeat = getSeatByInitializeCard(Card.SEER);
            Seat robberSeat = getSeatByInitializeCard(Card.ROBBER);
            Seat troublemakerSeat = getSeatByInitializeCard(Card.TROUBLEMAKER);
            Seat drunkSeat = getSeatByInitializeCard(Card.DRUNK);
            Seat insomniacSeat = getSeatByInitializeCard(Card.INSOMNIAC);
            //下面的ClientAction可能是投票
            //狼的回合
            if(singleWolfSeat != null){
                //场面上是一头孤狼
                XskrMessage message = new XskrMessage(String.format("看到桌面牌垛中第%s张牌是: %s", singleWolfCheckDesktopCard, getDisplayName(desktopCards.get(singleWolfCheckDesktopCard))), null, null);
                sendMessage(singleWolfSeat.getUserName(), message);
                keepKeyMessage(singleWolfSeat, message);
            }else if(wolf1Seat != null && wolf2Seat != null){
                //有两个狼玩家
                String messageTemplate = "看到狼人伙伴%s号玩家'%s'，同时他也看到了你。";
                XskrMessage wolf1Message = new XskrMessage(String.format(messageTemplate, getLocation(wolf2Seat), wolf2Seat.getUserName()), null, null);
                XskrMessage wolf2Message = new XskrMessage(String.format(messageTemplate, getLocation(wolf1Seat), wolf1Seat.getUserName()), null, null);
                sendMessage(wolf1Seat.getUserName(), wolf1Message);
                sendMessage(wolf2Seat.getUserName(), wolf2Message);
                keepKeyMessage(wolf1Seat, wolf1Message);
                keepKeyMessage(wolf2Seat, wolf2Message);
            }else if(wolf1Seat == null && wolf2Seat == null){
                //场面上没有狼，不需要给任何狼发消息
            }

            // 爪牙的回合
            if(minionSeat != null){
                String message;
                if(wolf1Seat != null && wolf2Seat != null){
                    //双狼
                    message = String.format("看到两头狼，%s号玩家'%s'和%s号玩家'%s'。",
                            getLocation(wolf1Seat), wolf1Seat.getUserName(), getLocation(wolf2Seat), wolf2Seat.getUserName());
                }else if(singleWolfSeat != null){
                    //孤狼
                    message = String.format("看到了一头孤狼，%s号玩家'%s'。",
                            getLocation(singleWolfSeat), singleWolfSeat.getUserName());
                }else{
                    //无狼
                    message = "场面上没有狼。";
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(minionSeat.getUserName(), xskrMessage);
                keepKeyMessage(minionSeat, xskrMessage);
            }

            // 守夜人的回合
            if(meson1Seat == null && meson2Seat != null){
                //单守夜
                XskrMessage xskrMessage = new XskrMessage("单人守夜没有同伴。", null, null);
                sendMessage(meson2Seat.getUserName(), xskrMessage);
                keepKeyMessage(meson2Seat, xskrMessage);
            }else if(meson1Seat != null && meson2Seat == null){
                XskrMessage xskrMessage = new XskrMessage("单人守夜没有同伴。", null, null);
                sendMessage(meson1Seat.getUserName(), xskrMessage);
                keepKeyMessage(meson1Seat, xskrMessage);
            }else if(meson1Seat != null && meson2Seat != null){
                //双守夜
                String messageTemplate = "看到另一位守夜人，%s号玩家'%s'。";
                XskrMessage meson1Message = new XskrMessage(String.format(messageTemplate, getLocation(meson2Seat), meson2Seat.getUserName()), null, null);
                XskrMessage meson2Message = new XskrMessage(String.format(messageTemplate, getLocation(meson1Seat), meson1Seat.getUserName()), null, null);
                sendMessage(meson1Seat.getUserName(), meson1Message);
                sendMessage(meson2Seat.getUserName(), meson2Message);
                keepKeyMessage(meson1Seat, meson1Message);
                keepKeyMessage(meson2Seat, meson2Message);
            }else{
                //无守夜
            }

            //预言家回合
            if(seerSeat != null){
                //查看一位玩家
                String message = "";
                if(seerCheckPlayerSeat != null){
                    Seat player = seats.get(seerCheckPlayerSeat);
                    message = String.format("查看%s号玩家'%s'的身份是: %s",
                            getLocation(player), player.getUserName(), getDisplayName(player.getCard()));
                }else{
                    Card card1 = desktopCards.get(seerCheckDesktopCard1);
                    Card card2 = desktopCards.get(seerCheckDesktopCard2);
                    String card1Name = getDisplayName(card1);
                    String card2Name = getDisplayName(card2);
                    message = String.format("翻开桌上第%s和%s张卡牌，看到了%s和%s", seerCheckDesktopCard1, seerCheckDesktopCard2, card1Name, card2Name);
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(seerSeat.getUserName(), xskrMessage);
                keepKeyMessage(seerSeat, xskrMessage);
            }

            if(robberSeat != null){
                Seat player = seats.get(robberSnatchPlayerSeat);
                Card swapCard = player.getCard();
                player.setCard(robberSeat.getCard());
                robberSeat.setCard(swapCard);
                String message = String.format("交换了%s号玩家'%s'的身份牌%s。",
                        getLocation(player), player.getUserName(), getDisplayName(swapCard));
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(robberSeat.getUserName(), xskrMessage);
                keepKeyMessage(robberSeat, xskrMessage);
            }
            if(troublemakerSeat != null){
                Seat player1 = seats.get(troublemakerExchangePlayerSeat1);
                Seat player2 = seats.get(troublemakerExchangePlayerSeat2);
                Card swapCard = player1.getCard();
                player1.setCard(player2.getCard());
                player2.setCard(swapCard);
                String message = String.format("交换了%s号玩家'%s'和%s号玩家'%s'的身份牌。",
                        getLocation(player1), player1.getUserName(), getLocation(player2), player2.getUserName());
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(troublemakerSeat.getUserName(), xskrMessage);
                keepKeyMessage(troublemakerSeat, xskrMessage);
            }
            if(drunkSeat != null){
                Card swapCard = desktopCards.get(drunkExchangeDesktopCard);
                desktopCards.put(drunkExchangeDesktopCard, drunkSeat.getCard());
                drunkSeat.setCard(swapCard);
                String message = String.format("交换了牌垛里的第%s张牌。", drunkExchangeDesktopCard);
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(drunkSeat.getUserName(), xskrMessage);
                keepKeyMessage(drunkSeat, xskrMessage);
            }
            if(insomniacSeat != null){
                String message;
                if(insomniacSeat.getCard() == Card.INSOMNIAC){
                    message = "牌没有被换过。";
                }else{
                    message = String.format("牌被换为%s。", getDisplayName(insomniacSeat.getCard()));
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(insomniacSeat.getUserName(), xskrMessage);
                keepKeyMessage(insomniacSeat, xskrMessage);
            }
            XskrMessage xskrMessage = new XskrMessage("进行三轮讨论后请投票。", ClientAction.VOTE_ACTION, null);
            sendMessage(xskrMessage);
            for(int i=0;i<getAvailableSeatCount();i++){
                Seat playerSeat = seats.get(i);
                keepKeyMessage(playerSeat, xskrMessage);
            }
            scene = Scene.VOTE;
        }
    }

    //接受玩家投票，计算，并公布获胜信息
    public void vote(String userName, int seat){
        logger.debug("vote(userName = {}, seat= {})", userName, seat);
        Seat playerSeat = getSeatByUserName(userName);
        if(playerSeat.getVoteSeat() == null) {
            playerSeat.setVoteSeat(seat);
            //票数在finishGame时统计，这里仅做投票
            if (canStatVote()) {
                finishGame();
            }
        }
    }

    //如果所有玩家都已经投票，那么可以统计投票数
    private boolean canStatVote(){
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVoteSeat() == null){
                return false;
            }
        }
        return true;
    }

    //如果统计的最高获票玩家中包含猎人，则由猎人独立投票，否则计算获胜阵营
    private VoteStat statVote(){
        //根据玩家的投票情况对每个玩家进行票数统计
        //并判断玩家队伍中是否存在狼
        boolean hasWolfInPlayers = false;
        boolean hasHunterInPlayers = false;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            int voteSeat = playerSeat.getVoteSeat();
            Seat votedPlayer = seats.get(voteSeat);
            votedPlayer.beVote();
            if(playerSeat.getCard() == Card.WEREWOLF_1 || playerSeat.getCard() == Card.WEREWOLF_2){
                hasWolfInPlayers = true;
            }
            if(playerSeat.getCard() == Card.HUNTER){
                hasHunterInPlayers = true;
            }
        }

        //找到被投票最多的票数
        int maxVoteCount = 0;
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVotedCount() > maxVoteCount){
                maxVoteCount = playerSeat.getVotedCount();
            }
        }

        //找到该投票次数的玩家
        Set<Seat> maxVotedPlayerSet = new HashSet();
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            if(playerSeat.getVotedCount() == maxVoteCount){
                maxVotedPlayerSet.add(playerSeat);
            }
        }
        return new VoteStat(hasWolfInPlayers, hasHunterInPlayers, maxVoteCount, maxVotedPlayerSet);
    }

    private void finishGame() {
        logger.debug("finishGame()");
        VoteStat voteStat = statVote();

        //分析获胜阵营: 狼人、村民、皮匠
        //按照如下顺序判定:
        //1. 如果玩家中没有狼人, 且每人得票数为1, 则共同获胜
        //2. 如果获得最大投票数的玩家中包含猎人身份，则由猎人获得当前投票结果后独立投票另一位玩家
        //3. 否则，如果只有皮匠获得最大票数，则皮匠阵营获胜
        //4. 否则，如果获得最大票数的角色中有狼，则村民获胜，皮匠和狼失败; 否则狼获胜，村民和皮匠失败

        // 统计并广播获胜阵营信息，游戏结束
        Set<Camp> victoryCamp = new TreeSet();
        Set<Camp> defeatCamp = new TreeSet();

        //只要皮匠被投出，皮匠即获胜
        if(voteStat.voted(Card.TANNER)){
            victoryCamp.add(Camp.TANNER);
        }else{
            defeatCamp.add(Camp.TANNER);
        }

        //根据玩家中有无狼来判断狼阵营和村阵营的输赢状况
        if (voteStat.hasWolfInPlayers()) {
            // 如果所有玩家中有狼
            if(voteStat.voted(Card.HUNTER)){
                // 如果有猎人被投中，则触发猎人技能

                // 告知猎人当前投票信息， 提示猎人由他独立投票
                StringBuilder report = new StringBuilder();
                for(int i=0;i<getAvailableSeatCount();i++){
                    Seat playerSeat = seats.get(i);
                    Seat votedPlayer = seats.get(playerSeat.getVoteSeat());
                    report.append(getLocation(playerSeat));
                    report.append("号玩家'");
                    report.append(playerSeat.getUserName());
                    report.append("'投");
                    report.append(playerSeat.getVoteSeat());
                    report.append("号玩家'");
                    report.append(votedPlayer.getUserName());
                    report.append("'\n");
                }
                //广播当前投票信息
                sendMessage(new XskrMessage(report.toString(), null, null));
                Seat hunter = getPlayerSeatByCard(Card.HUNTER);
                hunterVote = true;
                //TODO 数据部分需要所有玩家信息？
                XskrMessage hunterMessage = new XskrMessage("请投票", ClientAction.HUNTER_VOTE_ACTION, seats);
                sendMessage(hunter.getUserName(), hunterMessage);
                keepKeyMessage(hunter, new XskrMessage(report.toString(), ClientAction.HUNTER_VOTE_ACTION, seats));
                return ;
            }else if(voteStat.voted(Card.WEREWOLF_1) || voteStat.voted(Card.WEREWOLF_2)){
                victoryCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }else{
                victoryCamp.add(Camp.WOLF);
                defeatCamp.add(Camp.VILLAGER);
            }
        }else{
            //如果没有狼
            if (voteStat.getMaxVoteCount() == 1) {
                //共同获胜
                victoryCamp.add(Camp.VILLAGER);
                victoryCamp.add(Camp.WOLF);
            }else{
                //共同失败
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }
        }
        gameFinish(victoryCamp);
    }

    private void gameFinish(Set<Camp> victoryCamp) {
        //生成游戏结局
        List<Summary> summaries = new ArrayList();
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            Card card = playerSeat.getCard();
            Card initializeCard = playerSeat.getInitializeCard();
            Camp camp = Camp.getCamp(card);
            boolean outcome = victoryCamp.contains(camp);
            Summary summary = new Summary(camp, i, outcome, initializeCard, card, null);
            summaries.add(summary);
        }
        //解除所有玩家的准备状态，本局游戏结束
        for(int i=0;i<getAvailableSeatCount();i++){
            Seat playerSeat = seats.get(i);
            playerSeat.reset();
        }
        hunterVote = false;
        //TODO 通知所有客户端
        XskrMessage unreadyMessage = new XskrMessage("本局结束， 请勾选‘准备’进入下一局...", ClientAction.GAME_FINISH, summaries);
        sendMessage(unreadyMessage);
        //游戏进入停止状态，可以重新准备触发下一轮开始
        scene = Scene.PREPARE;
    }

    //猎人投票
    public void hunterVote(String userName, int seat){
        //TODO 判定该事件是否能够触发
        Seat hunterPlayer = getSeatByUserName(userName);
        if(hunterPlayer.getCard() == Card.HUNTER) {
            if (hunterVote) {
                logger.debug("hunterVote(userName = {}, seat = {})", userName, seat);
                Seat playerSeat = seats.get(seat);
                Set<Camp> victoryCampSet = new TreeSet();
                if (playerSeat.getCard() == Card.TANNER) {
                    sendMessage(new XskrMessage("皮匠获胜", null, null));
                    victoryCampSet.add(Camp.TANNER);
                } else if (playerSeat.getCard() == Card.WEREWOLF_1 || playerSeat.getCard() == Card.WEREWOLF_2) {
                    sendMessage(new XskrMessage("村民阵营获胜", null, null));
                    victoryCampSet.add(Camp.VILLAGER);
                } else {
                    sendMessage(new XskrMessage("狼人阵营获胜", null, null));
                    victoryCampSet.add(Camp.WOLF);
                }
                gameFinish(victoryCampSet);
            }else{
                logger.error("非猎人技能触发时机，试图进行猎人投票");
            }
        }else{
            logger.error("非猎人玩家试图进行猎人投票.");
        }
    }

    /**
     * 房主点击某个卡，来调整房间的座位以及卡牌设定
     * @param userName
     * @param card
     */
    public void pickCard(String userName, Card card){
        if(userName.equals(owner)){
            if(scene == Scene.PREPARE){
                //TODO 房主修改房间卡牌设定
            }else{
                throw new RuntimeException("只有准备阶段才能修改房间卡牌设定。");
            }
        }else{
            throw new RuntimeException("只有房主才能调整房间卡牌设定。");
        }
    }

    public synchronized void pickDesktopCard(String userName, int location){
        logger.debug("pickDesktopCard(userName={}, card={})", userName, location);
        Seat playerSeat = getSeatByUserName(userName);
        //如果卡牌序号信息是合理的
        if (location >= 0 && location < TABLE_DECK_THICKNESS) {
            if(scene == Scene.ACTIVATE) {
                if (playerSeat.getInitializeCard() == Card.SEER) {
                    if (seerCheckPlayerSeat == null) {
                        if (seerCheckDesktopCard1 == null) {
                            //预言家尚未验证第一张牌
                            seerCheckDesktopCard1 = location;
                            //TODO 返回"请再选择另外一张"的消息
                        } else if (seerCheckDesktopCard2 == null && location != seerCheckDesktopCard1) {
                            //预言家验证了第一张牌，但尚未验证第二张， 且此张非第一张
                            seerCheckDesktopCard2 = location;
                            attemptNightAction();
                            //TODO 返回"请等待所有玩家行动结束，系统会给出下一步指示"的消息
                        } else {
                            //do nothing
                        }
                    } else {
                        //do nothing
                    }
                } else if (getSingleWolfSeat() != null &&
                        (playerSeat.getInitializeCard() == Card.WEREWOLF_1 ||
                                playerSeat.getInitializeCard() == Card.WEREWOLF_2)) {
                    if (singleWolfCheckDesktopCard == null) {
                        singleWolfCheckDesktopCard = location;
                        if (desktopCards.get(location) == Card.WEREWOLF_2
                                || desktopCards.get(location) == Card.WEREWOLF_1) {
                            //TODO 返回"是狼牌可以再验一张的消息"

                        } else {
                            singleWolfCheckDesktopCard = location;
                            attemptNightAction();
                        }
                    }
                } else if (playerSeat.getInitializeCard() == Card.DRUNK) {
                    if (drunkExchangeDesktopCard == null) {
                        drunkExchangeDesktopCard = location;
                        //TODO 返回"请等待所有玩家行动结束，系统会给出下一步指示"的消息
                        attemptNightAction();
                    }
                } else {
                    //do nothing
                }
            }else if(scene == Scene.VOTE){
                //非行动时间进行行动请求
                //do nothing
            } else if(scene == Scene.PREPARE){
                //do nothing
            }else{
                throw new RuntimeException("Unsupported scene: " + scene);
            }
        } else {
            throw new RuntimeException("卡牌序号" + location + "不合理");
        }
    }

    public synchronized void pickSeat(String userName, int location){
        logger.debug("pickSeat(userName={}, seat={}) scene={}", userName, location, scene);
        Seat playerSeat = getSeatByUserName(userName);
        //验证座位的合理性
        if (location >= 0 && location < getAvailableSeatCount()) {
            if (scene == Scene.ACTIVATE) {
                if(playerSeat.getUserName() != null) {
                    if (location != getLocation(playerSeat)) {
                        if (playerSeat.getInitializeCard() == Card.SEER) {
                            //预言家没有验过牌也没有验过人且所验玩家不是自己
                            if (seerCheckDesktopCard1 == null && seerCheckDesktopCard2 == null && seerCheckPlayerSeat == null && location != getLocation(playerSeat)) {
                                seerCheckPlayerSeat = location;
                                //TODO 返回等待消息
                                attemptNightAction();
                            } else {
                                //do nothing
                            }
                        } else if (playerSeat.getInitializeCard() == Card.ROBBER) {
                            //强盗还没抢过人
                            if (robberSnatchPlayerSeat == null) {
                                robberSnatchPlayerSeat = location;
                                //TODO 返回等待消息
                                attemptNightAction();
                            } else {
                                //do nothing
                            }
                        } else if (playerSeat.getInitializeCard() == Card.TROUBLEMAKER) {
                            if (location != getLocation(playerSeat)) {
                                if (troublemakerExchangePlayerSeat1 == null &&
                                        location != getLocation(playerSeat)) {
                                    //不能换自己的牌
                                    troublemakerExchangePlayerSeat1 = location;
                                    //TODO 返回再选一张消息
                                } else if (troublemakerExchangePlayerSeat2 == null &&
                                        location != getLocation(playerSeat) &&
                                        location != troublemakerExchangePlayerSeat1) {
                                    //不能换自己的牌且不能与第一张选中的牌相同
                                    troublemakerExchangePlayerSeat2 = location;
                                    //TODO 返回等待消息
                                    attemptNightAction();
                                } else {
                                    //do nothing
                                }
                            } else {
                                //do nothing
                            }
                        }
                    } else {
                        //do nothing
                    }
                }else{
                    //玩家不在房间内无法行动
                    String message = String.format("玩家%s不在房间内无法行动。", userName);
                    throw new RuntimeException(message);
                }
            } else if (scene == Scene.VOTE) {
                if(playerSeat != null) {
                    if(playerSeat.getUserName() != null) {
                        if (hunterVote) {
                            if (playerSeat.getInitializeCard() == Card.HUNTER) {
                                //猎人技能投票
                                hunterVote(userName, location);
                            } else {
                                //do nothing
                            }
                        } else {
                            //普通vote
                            playerSeat.setVoteSeat(location);
                            if (canStatVote()) {
                                finishGame();
                            }
                        }
                    }else{
                        throw new RuntimeException(String.format("%s未在座位上不能触发行动。", userName));
                    }
                }else{
                    //玩家不在房间内无法行动
                    String message = String.format("玩家%s不在房间内无法投票。", userName);
                    throw new RuntimeException(message);
                }
            } else if(scene == Scene.PREPARE){
                //准备状态点击座位，表示交换座位
                Seat targetSeat = seats.get(location);
                if(playerSeat != null){
                    //如果是玩家，存在离开座位、换到空位，两种情况
                    //如果已经准备了就不能再换位子了
                    if(playerSeat.isReady()){
                        //如果该玩家已经准备了不能换座位,提醒他一下
                        XskrMessage xskrMessage = new XskrMessage("已经准备不能换座", null, null);
                        sendMessage(playerSeat.getUserName(), xskrMessage);
                    }else{
                        if(targetSeat.getUserName() == null){
                            //换到空座位
                            String tempUserName = playerSeat.getUserName();
                            playerSeat.setUserName(null);
                            targetSeat.setUserName(tempUserName);
                            //发送一个刷新房间信息的(换座)事件
                            String message = String.format("换座位到%s", location);
                            ClientAction clientAction = ClientAction.ROOM_CHANGED;
                            Object data = this;
                            XskrMessage roomChangedMessage = new XskrMessage(message, clientAction, data);
                            sendMessage(roomChangedMessage);
                        }else if(playerSeat == targetSeat){
                            //玩家离开座位
                            playerSeat.setUserName(null);
                            observers.add(userName);
                            //发送一个刷新房间信息的(换座)事件
                            String message = String.format("%s离开座位%s", userName, location);
                            ClientAction clientAction = ClientAction.ROOM_CHANGED;
                            Object data = this;
                            XskrMessage roomChangedMessage = new XskrMessage(message, clientAction, data);
                            //发消息给所有用户
                            sendMessage(roomChangedMessage);
                        }else{
                            //该座位已经有人了, 可以在客户端判断一下，减少通讯
                            String message = String.format("座位%s已经有玩家%s.", location, targetSeat.getUserName());
                            logger.warn(message);
                        }
                    }
                }else if(isObserver(userName)){
                    System.out.println("------------------------------");
                    System.out.println(JSON.toJSONString(this));
                    //如果请求换座位的是观察者
                    if(targetSeat.getUserName() == null){
                        //如果请求的座位没有人坐，则观察者坐座位变为玩家
                        targetSeat.setUserName(userName);
                        observers.remove(userName);
                        String message = String.format("选择%s号座位", location);
                        ClientAction clientAction = ClientAction.ROOM_CHANGED;
                        Object data = this;
                        XskrMessage roomChangedMessage = new XskrMessage(message, clientAction, data);
                        sendMessage(roomChangedMessage);
                    }else{
                        //该座位已经有人了, 可以在客户端判断一下，减少通讯
                        String message = String.format("座位%s已经有玩家%s.", location, targetSeat.getUserName());
                        logger.warn(message);
                    }
                }else{
                    String message = String.format("用户%s既不属于观看者也不属于玩家。", userName);
                    throw new RuntimeException(message);
                }
            }else {
                throw new RuntimeException("不支持的场景: " + scene);
            }
        } else {
            String message = String.format("座位序号%s不合理，应取值在%s到%s之间。", location, 0, getAvailableSeatCount() - 1);
            logger.warn(message);
        }
    }

    private boolean isObserver(String userName){
        return observers.contains(userName);
    }

    public int getLocation(Seat seat){
        for(int i=0;i<seats.size();i++){
            if(seat == seats.get(i)){
                return i;
            }
        }
        throw new RuntimeException("无法找到座位: " + seat);
    }

    public void setSimpMessagingTemplate(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    private void sendMessage(String userName, XskrMessage message){
        String roomWebSocketQueue = "/queue";
        if(simpMessagingTemplate != null){
            System.out.println("send queue: " + roomWebSocketQueue);
            simpMessagingTemplate.convertAndSendToUser(userName, roomWebSocketQueue, message);
        }else{
            System.out.println(String.format("sendMessage to %s: %s", userName, JSON.toJSONString(message, true)));
        }
    }

    private void sendMessage(XskrMessage message){
        String roomWebSocketTopic = "/topic/" + id;
        if(simpMessagingTemplate != null) {
            System.out.println("send topick: " + roomWebSocketTopic);
            simpMessagingTemplate.convertAndSend(roomWebSocketTopic, message);
        }else{
            System.out.println(String.format("sendMessage to All: %s", JSON.toJSONString(message, true)));
        }
    }

    public int getID() {
        return id;
    }

    public Card[] getCards(){
        return cards;
    }

    public void setCards(Card[] cards) {
        if(scene == Scene.PREPARE) {
            this.cards = cards;
            XskrMessage roomChangedMessage = new XskrMessage(null, ClientAction.ROOM_CHANGED, this);
            sendMessage(roomChangedMessage);
        }
    }

    private void keepKeyMessage(Seat player, XskrMessage xskrMessage){
        player.getKeyMessages().add(xskrMessage);
    }

    public List<String> getKeyMessages(String userName){
        Seat playerSeat = getSeatByUserName(userName);
        if(playerSeat != null) {
            List<XskrMessage> xskrMessages = playerSeat.getKeyMessages();
            List<String> messages = new ArrayList();
            if (xskrMessages != null) {
                for (XskrMessage xskrMessage : xskrMessages) {
                    messages.add(xskrMessage.getMessage());
                }
            }
            return messages;
        }else{
            return null;
        }
    }
//    public ClientAction getLastClientAction(String userName){
//        Seat playerSeat = getSeatByUserName(userName);
//        if(playerSeat != null) {
//            return playerSeat.getLastAction();
//        }else{
//            return null;
//        }
//    }

    private String getDisplayName(Card card){
        switch(card){
            case WEREWOLF_1: return "狼人";
            case WEREWOLF_2: return "狼人";
            case MINION: return "爪牙";
            case TANNER: return "皮匠";
            case MASON_1: return "守夜人";
            case MASON_2: return "守夜人";
            case DRUNK: return "酒鬼";
            case HUNTER: return "猎人";
            case INSOMNIAC: return "失眠者";
            case ROBBER: return "强盗";
            case SEER: return "预言家";
            case TROUBLEMAKER: return "捣蛋鬼";
            case VILLAGER_1: return "村民";
            case VILLAGER_2: return "村民";
            case VILLAGER_3: return "村民";
            default: return card.toString();
        }
    }

    public String getOwner() {
        return owner;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public Set<String> getObservers() {
        return observers;
    }
}
