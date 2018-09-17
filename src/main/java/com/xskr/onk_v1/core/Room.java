package com.xskr.onk_v1.core;

import com.alibaba.fastjson.JSON;
import com.xskr.common.XskrMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

public class Room {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final int TABLE_DECK_THICKNESS = 3;

    //房间号
    private int id;

    // 该房间支持的所有卡牌
    private Card[] cards;

    // 进入房间的玩家，但可能还没有入座
    private Set<Player> players;
    // 发牌后剩余的桌面3张牌垛
    private TreeMap<Integer, Card> tableDeck = new TreeMap();


    // 玩家姓名到玩家的索引
    private Map<String, Player> namePlayerMap;
    // 发牌后卡牌到玩家索引(原始身份)
    private Map<Card, Player> initializeCardPlayerMap;
    // 玩家就坐后座位号到玩家索引
    private Map<Integer, Player> seatPlayerMap;
    // 本局做过的操作

    // 玩家夜间操作
    private Integer singleWolfCheckDeck;
    private Integer seerCheckDeck1;
    private Integer seerCheckDeck2;
    private Integer seerCheckPlayer;
    private Integer robberSnatchSeat;
    private Integer troublemakerExchangeSeat1;
    private Integer troublemakerExchangeSeat2;
    private Integer drunkExchangeDeck;
    private boolean hunterVote = false;

    //用于发送WebSocket信息
    private SimpMessagingTemplate simpMessagingTemplate;

    //游戏是否处于开始状态，如果是就不能再进行准备切换
    private boolean gaming = false;

    //用于客户端识别当前登录用户, TODO 应由框架引入
    private String currentPlayerName;
    //存放客户端的关键信息，供断线重连后查看
    private Map<Player, List<XskrMessage>> playerMessagesMap;
    //存放玩家最后一次行动信息，供断线重连后操作
    private Map<Player, ClientAction> playerClientActionMap;
    //存放单个玩家最后一次行动信息
    private ClientAction clientAction;

    public Room(int id, List<Card> cardList){
        logger.debug("Room(cardList = {})", Arrays.toString(cardList.toArray()));
        this.id = id;
        this.cards = cardList.toArray(new Card[0]);
        int seatCount = getSeatCount();
        this.players = new TreeSet();

        this.seatPlayerMap = new HashMap(seatCount);
        this.namePlayerMap = new HashMap(seatCount);
        this.initializeCardPlayerMap = new HashMap(seatCount);
        this.playerMessagesMap = new HashMap(seatCount);
        this.playerClientActionMap = new HashMap(seatCount);
    }

    public int getSeatCount(){
        return cards.length - TABLE_DECK_THICKNESS;
    }

    /**
     * 玩家进入房间
     * @param playerName
     * @return 返回玩家座位
     */
    public void join(String playerName){
        logger.debug("join(playerName = {})", playerName);
        Player player = namePlayerMap.get(playerName);
        if(player != null){
            //玩家已经在该房间, 断线重连
        }else{
            player = new Player(playerName);
            //玩家进入该房间
            if(players.size() < getSeatCount()){
                //房间未满
                //为玩家分配一个座位, 从0到最大座位数遍历，发现一个座位没有人便分配
                for(int i=1;i<=getSeatCount();i++){
                    if(seatPlayerMap.get(i) == null){
                        seatPlayerMap.put(i, player);
                        namePlayerMap.put(playerName, player);
                        player.setSeat(i);
                        players.add(player);
                        break;
                    }
                }
                String messageContent = String.format("%s加入房间。", player.getName());
                XskrMessage xskrMessage = new XskrMessage(messageContent, ClientAction.REFRESH_PLAYERS_INFO_ACTION, null);
                sendMessage(xskrMessage);
            }else{
                throw new RuntimeException("房间已满");
            }
        }
    }

    /**
     * 玩家离开房间
     * @param playerName
     */
    public void leave(String playerName){
        logger.debug("leave(playerName = {})", playerName);
        Player player = namePlayerMap.get(playerName);
        if(player != null) {
            players.remove(player);
            //TODO 需要清理索引,用户在游戏进行时离开会导致游戏无法继续
            namePlayerMap.remove(playerName);
            initializeCardPlayerMap.remove(player.getCard());
            seatPlayerMap.remove(player.getSeat());
        }
    }

    /**
     * 玩家坐到指定位置, 当所有玩家都准备好时游戏自动开始
     * TODO 或许应该加个倒数5,4,3,2,1
     * @param playerName 玩家
     * @return 该玩家最终处于的ready状态
     */
    public boolean setReady(String playerName, boolean ready){
        if(gaming) {
            return true;
        }else{
            logger.debug("setReady(playerName = {}, ready = {})", playerName, ready);
            Player player = namePlayerMap.get(playerName);
            if(player != null) {
                try {
                    //设定该玩家的ready状态
                    player.setReady(ready);
                    return ready;
                }finally{
                    //检查是否能够触发游戏开始事件
                    //如果玩家数量达到座位数量，且玩家都是ready状态则触发新游戏事件
                    if(players.size() == getSeatCount()) {
                        boolean newGame = true;
                        for (Player player1 : players) {
                            if (!player1.isReady()) {
                                newGame = false;
                                break;
                            }
                        }
                        if(newGame) {
                            newGame();
                        }
                    }else{
                        //人数未达到座位数游戏无法开始
                    }
                }
            }else{
                String message = String.format("玩家%s不在该房间，无法设定准备状态。", playerName);
                throw new RuntimeException(message);
            }
        }
    }

    public boolean sit(String playerName, int seat){
        logger.debug("sit(playerName = {}, seat = {})", playerName, seat);
        //检查seat number的合法性， 从1开始到玩家数量上限
        if(seat>0 && seat<=getSeatCount()){
            Player player = namePlayerMap.get(playerName);
            if(player != null){
                if(!seatPlayerMap.keySet().contains(seat)){
                    seatPlayerMap.put(seat, player);
                    player.setSeat(seat);
                    return true;
                }else{
                    // 这个座位已经有人了
                }
            }else{
                String message = String.format("玩家%s不在房间, 无法应用座位号。", playerName);
                throw new RuntimeException(message);
            }
        }else{
            String message = String.format("试图为玩家%s分配不合理的座位号: %d", playerName, seat);
            throw new RuntimeException(message);
        }
        return false;
    }

    public Set<Player> getPlayers(){
        return players;
    }

    public Player getPlayerByName(String name){
        return namePlayerMap.get(name);
    }

    /**
     * 根据初始卡牌查询用户
     * @param card
     * @return
     */
    public Player getPlayerByInitializeCard(Card card){
        return initializeCardPlayerMap.get(card);
    }

    /**
     * 根据卡牌查询用户，卡牌是夜晚行动过后被交换过的
     * @param card
     * @return
     */
    public Player getPlayerByCard(Card card){
        for(Player player:players){
            if(player.getCard() == card){
                return player;
            }
        }
        return null;
    }

    public Player getPlayerBySeat(int seat){
        return seatPlayerMap.get(seat);
    }
    /**
     * 初始化一局游戏
     */
    private void newGame(){
        //标记进入游戏状态
        gaming = true;
        logger.debug("newGame()");
        XskrMessage xskrMessage = new XskrMessage("新一局开始了！", null, null);
        sendMessage(xskrMessage);

        Deck deck = new Deck(cards);

        //检查玩家数量与预期数量一致, 不能开始游戏
        if(players.size() != getSeatCount()){
            return;
//            throw new RuntimeException(String.format("玩家数量%s与座位数量%s不符!!", players.size(), getSeatCount()));
        }

        //检查玩家是否都已经就坐, 座位号是否符合逻辑
        for(int i = 1; i <= getSeatCount(); i++){
            int seatNumber = i;
            Player player = seatPlayerMap.get(seatNumber);
            if(player != null) {
                if (player.getSeat() != seatNumber) {
                    throw new RuntimeException(String.format("玩家%s的座位号不符合逻辑.", JSON.toJSONString(player)));
                }
            }else{
                throw new RuntimeException(String.format("座位%s处没有玩家.", i));
            }
        }

        //清空上一局所有角色的操作状态
        singleWolfCheckDeck = null;
        seerCheckDeck1 = null;
        seerCheckDeck2 = null;
        seerCheckPlayer = null;
        robberSnatchSeat = null;
        troublemakerExchangeSeat1 = null;
        troublemakerExchangeSeat2 = null;
        drunkExchangeDeck = null;
        //清空上一局所有玩家的身份与投票状态
        for(Player player:players){
            player.setCard(null);
        }
        //清空每个玩家身上的被投票状态
        clearVote();

        //洗牌
        deck.shuffle(500);

        //为所有人发牌，并建立牌到玩家的索引
        initializeCardPlayerMap.clear();
        for(Player player:players){
            Card card = deck.deal();
            player.setCard(card);
            initializeCardPlayerMap.put(card, player);
        }

        //建立桌面剩余的牌垛
        tableDeck.put(1, deck.deal());
        tableDeck.put(2, deck.deal());
        tableDeck.put(3, deck.deal());

        logger.debug("playerAction");
        //需要主动行动的玩家
        Player singleWolf = getSingleWolf();
        Player seer = initializeCardPlayerMap.get(Card.SEER);
        Player robber = initializeCardPlayerMap.get(Card.ROBBER);
        Player troublemaker = initializeCardPlayerMap.get(Card.TROUBLEMAKER);
        Player drunk = initializeCardPlayerMap.get(Card.DRUNK);

        //发牌结束后根据身份为每个玩家发送行动提示信息
        String playerSeatRange = "1--" + players.size();
        System.out.println(playerSeatRange);
        for(Map.Entry<Card, Player> entry: initializeCardPlayerMap.entrySet()){
            Player player = entry.getValue();
            if(player != null) {
                String message;
                ClientAction clientAction = null;
                if(player == singleWolf){
                    message = String.format("请输入1-3中的一个数字，来查看牌垛中对应的纸牌");
                    clientAction = ClientAction.SINGLE_WOLF_ACTION;
                }else if (player == seer) {
                    message = String.format("请输入1-3中的两个数字来查看的牌垛中对应的纸牌，或者输入所有玩家(%s)的座位号之一，逗号分隔。", 1, playerSeatRange);
                    clientAction = ClientAction.SEER_ACTION;
                }else if(player == robber){
                    message = String.format("请输入所有玩家(%s)的座位号之一，查看并交换该身份。", playerSeatRange);
                    clientAction = ClientAction.ROBBER_ACTION;
                }else if(player == troublemaker){
                    message = String.format("请输入除您(%s)之外两个玩家(%s)的座位号，交换他们的身份。", player.getSeat(), playerSeatRange);
                    clientAction = ClientAction.TROUBLEMAKER_ACTION;
                }else if(player == drunk){
                    message = String.format("请输入牌垛中三张纸牌1-3中的一个号码，与之交换身份卡。");
                    clientAction = ClientAction.DRUNK_ACTION;
                }else{
                    message = "所有玩家行动完成后，系统会给出下一步的信息。";
                }
                //向玩家发送身份提示信息
                XskrMessage xskrMessage1 = new XskrMessage(String.format("您的初始身份是%s。", player.getCard().getDisplayName()), clientAction, null);
                //向玩家发送操作提示信息
                XskrMessage xskrMessage2 = new XskrMessage(message, null, null);
                sendMessage(player, xskrMessage1);
                sendMessage(player, xskrMessage2);
                keepKeyMessage(player, xskrMessage1);
                keepKeyMessage(player, xskrMessage2);
            }
        }
    }

    public TreeMap<Integer, Card> getTableDeck() {
        return tableDeck;
    }

    //是否能够进入白天，当所有需要操作的玩家行动完毕才能进入白天
    private boolean canNightAction(){
        logger.debug("canNightAction()");

        // 检查孤狼是否已经行动
        if(getSingleWolf() != null){
            //如果存在孤狼的角色
            if(singleWolfCheckDeck == null){
                //如果孤狼没有指定要看的牌垛中的一张牌
                logger.debug("singleWolf not work!");
                return false;
            }
        }

        //检查预言家是否已经行动
        if(initializeCardPlayerMap.get(Card.SEER) != null){
            //如果存在预言家角色
            if((seerCheckDeck1 == null || seerCheckDeck2 == null) &&
                    seerCheckPlayer == null){
                //如果语言家既没有指定要看牌垛中的那两张牌，也没有指定要验证的玩家的身份
                logger.debug("seer not work!");
                return false;
            }
        }

        // 检查强盗是否已经行动
        if(initializeCardPlayerMap.get(Card.ROBBER) != null){
            //如果存在强盗玩家
            if(robberSnatchSeat == null){
                // 如果强盗没有指定要交换的位置
                logger.debug("robber not work!");
                return false;
            }
        }

        //检查捣蛋鬼是否已经行动
        if(initializeCardPlayerMap.get(Card.TROUBLEMAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troublemakerExchangeSeat1 == null
                    || troublemakerExchangeSeat2 == null){
                //如果捣蛋鬼没有指定要交换的位置
                logger.debug("troublemaker not work!");
                return false;
            }
        }

        //检查酒鬼是否行动
        if(initializeCardPlayerMap.get(Card.DRUNK) != null){
            if(drunkExchangeDeck == null){
                logger.debug("drunk not work!");
                return false;
            }
        }

        //通过了所有的检查
        return true;
    }

    //获得孤狼玩家
    protected Player getSingleWolf(){
        Player wolf1 = initializeCardPlayerMap.get(Card.WEREWOLF_1);
        Player wolf2 = initializeCardPlayerMap.get(Card.WEREWOLF_2);
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
//          Player doopelganger = initializeCardPlayerMap.get(Card.DOPPELGANGER);
            Player singleWolf = getSingleWolf();
            Player wolf1 = initializeCardPlayerMap.get(Card.WEREWOLF_1);
            Player wolf2 = initializeCardPlayerMap.get(Card.WEREWOLF_2);
            Player minion = initializeCardPlayerMap.get(Card.MINION);
            Player meson1 = initializeCardPlayerMap.get(Card.MASON_1);
            Player meson2 = initializeCardPlayerMap.get(Card.MASON_2);
            Player seer = initializeCardPlayerMap.get(Card.SEER);
            Player robber = initializeCardPlayerMap.get(Card.ROBBER);
            Player troublemaker = initializeCardPlayerMap.get(Card.TROUBLEMAKER);
            Player drunk = initializeCardPlayerMap.get(Card.DRUNK);
            Player insomniac = initializeCardPlayerMap.get(Card.INSOMNIAC);
            //下面的ClientAction可能是投票
            //狼的回合
            if(singleWolf != null){
                //场面上是一头孤狼
                XskrMessage message = new XskrMessage(String.format("看到桌面牌垛中第%s张牌是: %s", singleWolfCheckDeck, tableDeck.get(singleWolfCheckDeck).getDisplayName()), null, null);
                sendMessage(singleWolf, message);
                keepKeyMessage(singleWolf, message);
            }else if(wolf1 != null && wolf2 != null){
                //有两个狼玩家
                String messageTemplate = "看到狼人伙伴%s号玩家'%s'看向你，露出了狡黠的目光。";
                XskrMessage wolf1Message = new XskrMessage(String.format(messageTemplate, wolf2.getSeat(), wolf2.getName()), null, null);
                XskrMessage wolf2Message = new XskrMessage(String.format(messageTemplate, wolf1.getSeat(), wolf1.getName()), null, null);
                sendMessage(wolf1, wolf1Message);
                sendMessage(wolf2, wolf2Message);
                keepKeyMessage(wolf1, wolf1Message);
                keepKeyMessage(wolf2, wolf2Message);
            }else if(wolf1 == null && wolf2 == null){
                //场面上没有狼，不需要给任何狼发消息
            }

            // 爪牙的回合
            if(minion != null){
                String message;
                if(wolf1 != null && wolf2 != null){
                    //双狼
                    message = String.format("看到两头狼，%s号玩家'%s'和%s号玩家'%s'。",
                            wolf1.getSeat(), wolf1.getName(), wolf2.getSeat(), wolf2.getName());
                }else if(singleWolf != null){
                    //孤狼
                    message = String.format("看到了一头孤狼，%s号玩家'%s'。",
                            singleWolf.getSeat(), singleWolf.getName());
                }else{
                    //无狼
                    message = "场面上没有狼。";
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(minion, xskrMessage);
                keepKeyMessage(minion, xskrMessage);
            }

            // 守夜人的回合
            if(meson1 == null && meson2 != null){
                //单守夜
                XskrMessage xskrMessage = new XskrMessage("没有同伴。", null, null);
                sendMessage(meson2, xskrMessage);
                keepKeyMessage(meson2, xskrMessage);
            }else if(meson1 != null && meson2 == null){
                XskrMessage xskrMessage = new XskrMessage("没有同伴。", null, null);
                sendMessage(meson1, xskrMessage);
                keepKeyMessage(meson1, xskrMessage);
            }else if(meson1 != null && meson2 != null){
                //双守夜
                String messageTemplate = "看到另一位守夜人，%s号玩家'%s'。";
                XskrMessage meson1Message = new XskrMessage(String.format(messageTemplate, meson2.getSeat(), meson2.getName()), null, null);
                XskrMessage meson2Message = new XskrMessage(String.format(messageTemplate, meson1.getSeat(), meson1.getName()), null, null);
                sendMessage(meson1, meson1Message);
                sendMessage(meson2, meson2Message);
                keepKeyMessage(meson1, meson1Message);
                keepKeyMessage(meson2, meson2Message);
            }else{
                //无守夜
            }

            //预言家回合
            if(seer != null){
                //查看一位玩家
                String message = "";
                if(seerCheckPlayer != null){
                    Player player = seatPlayerMap.get(seerCheckPlayer);
                    message = String.format("查看%s号玩家'%s'的身份是: %s",
                            player.getSeat(), player.getName(), player.getCard().getDisplayName());
                }else{
                    Card card1 = tableDeck.get(seerCheckDeck1);
                    Card card2 = tableDeck.get(seerCheckDeck2);
                    message = String.format("翻开桌上第%s和%s张卡牌，看到了%s和%s", seerCheckDeck1, seerCheckDeck2, card1, card2);
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(seer, xskrMessage);
                keepKeyMessage(seer, xskrMessage);
            }

            if(robber != null){
                Player player = seatPlayerMap.get(robberSnatchSeat);
                Card swapCard = player.getCard();
                player.setCard(robber.getCard());
                robber.setCard(swapCard);
                //更新缓存
                initializeCardPlayerMap.put(player.getCard(), player);
                initializeCardPlayerMap.put(robber.getCard(), robber);
                String message = String.format("交换了%s号玩家'%s'的身份牌%s。",
                        player.getSeat(), player.getName(), swapCard);
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(robber, xskrMessage);
                keepKeyMessage(robber, xskrMessage);
            }
            if(troublemaker != null){
                Player player1 = seatPlayerMap.get(troublemakerExchangeSeat1);
                Player player2 = seatPlayerMap.get(troublemakerExchangeSeat2);
                Card swapCard = player1.getCard();
                player1.setCard(player2.getCard());
                player2.setCard(swapCard);
                //更新缓存
                initializeCardPlayerMap.put(player1.getCard(), player1);
                initializeCardPlayerMap.put(player2.getCard(), player2);
                String message = String.format("交换了%s号玩家'%s'和%s号玩家'%s'的身份牌。",
                        player1.getSeat(), player1.getName(), player2.getSeat(), player2.getName());
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(troublemaker, xskrMessage);
                keepKeyMessage(troublemaker, xskrMessage);
            }
            if(drunk != null){
                Card swapCard = tableDeck.get(drunkExchangeDeck);
                tableDeck.put(drunkExchangeDeck, drunk.getCard());
                drunk.setCard(swapCard);
                //更新缓存
                initializeCardPlayerMap.put(drunk.getCard(), drunk);
                initializeCardPlayerMap.remove(swapCard, drunk);
                String message = String.format("交换了牌垛里的第%s张牌。", drunkExchangeDeck);
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(drunk, xskrMessage);
                keepKeyMessage(drunk, xskrMessage);
            }
            if(insomniac != null){
                String message;
                if(insomniac.getCard() == Card.INSOMNIAC){
                    message = "牌没有被换过。";
                }else{
                    message = String.format("牌被换为%s。", insomniac.getCard().getDisplayName());
                }
                XskrMessage xskrMessage = new XskrMessage(message, null, null);
                sendMessage(insomniac, xskrMessage);
                keepKeyMessage(insomniac, xskrMessage);
            }
            XskrMessage xskrMessage = new XskrMessage("进行三轮讨论后请投票。", ClientAction.VOTE_ACTION, null);
            sendMessage(xskrMessage);
            for(Player player:players){
                keepKeyMessage(player, xskrMessage);
            }
        }
    }

    //接受玩家投票，计算，并公布获胜信息
    public void vote(String playerName, int seat){
        logger.debug("vote(playerName = {}, seat= {})", playerName, seat);
        Player player = namePlayerMap.get(playerName);
        if(player.getVoteSeat() == null) {
            player.setVoteSeat(seat);
            //票数在finishGame时统计，这里仅做投票
            if (canStatVote()) {
                finishGame();
            }
        }
    }

    //如果所有玩家都已经投票，那么可以统计投票数
    private boolean canStatVote(){
        for(Player player: players){
            if(player.getVoteSeat() == null){
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
        for(Player player: players){
            int voteSet = player.getVoteSeat();
            Player votedPlayer = seatPlayerMap.get(voteSet);
            votedPlayer.beVote();
            if(player.getCard() == Card.WEREWOLF_1 || player.getCard() == Card.WEREWOLF_2){
                hasWolfInPlayers = true;
            }
            if(player.getCard() == Card.HUNTER){
                hasHunterInPlayers = true;
            }
        }

        //找到被投票最多的票数
        int maxVoteCount = 0;
        for(Player player:players){
            if(player.getVotedCount() > maxVoteCount){
                maxVoteCount = player.getVotedCount();
            }
        }

        //找到该投票次数的玩家
        Set<Player> maxVotedPlayerSet = new HashSet();
        for(Player player:players){
            if(player.getVotedCount() == maxVoteCount){
                maxVotedPlayerSet.add(player);
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
                for(Player player:players){
                    Player votedPlayer = seatPlayerMap.get(player.getVoteSeat());
                    report.append(player.getSeat());
                    report.append("号玩家'");
                    report.append(player.getName());
                    report.append("'投");
                    report.append(player.getVoteSeat());
                    report.append("号玩家'");
                    report.append(votedPlayer.getName());
                    report.append("'\n");
                }
                //广播当前投票信息
                sendMessage(new XskrMessage(report.toString(), null, null));
                Player hunter = getPlayerByCard(Card.HUNTER);
                hunterVote = true;
                XskrMessage hunterMessage = new XskrMessage("请投票", ClientAction.HUNTER_VOTE_ACTION, players);
                sendMessage(hunter, hunterMessage);
                keepKeyMessage(hunter, new XskrMessage(report.toString(), ClientAction.HUNTER_VOTE_ACTION, players));
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
        for(Player player:players){
            Card card = player.getCard();
            Card initializeCard = null;
            for(Map.Entry<Card, Player> entry: initializeCardPlayerMap.entrySet()){
                if(player == entry.getValue()){
                    initializeCard = entry.getKey();
                    break;
                }
            }
            Camp camp = Camp.getCamp(card);
            int seat = player.getSeat();
            boolean outcome = victoryCamp.contains(camp);
            Summary summary = new Summary(camp, seat, outcome, initializeCard, card, null);
            summaries.add(summary);
        }
        //清空每个玩家的关键消息
        playerMessagesMap.clear();
        playerClientActionMap.clear();
        sendMessage(new XskrMessage("请准备...", null, null));

        //解除所有玩家的准备状态，本局游戏结束
        for(Player player:players){
            player.setReady(false);
        }
        hunterVote = false;
        //TODO 通知所有客户端
        XskrMessage unreadyMessage = new XskrMessage("本局结束", ClientAction.UNREADY_ACTION, summaries);
        sendMessage(unreadyMessage);
        //游戏进入停止状态，可以重新准备触发下一轮开始
        gaming = false;
    }

    //TODO 检查是否已经行动过了
    //捣蛋鬼换牌
    public void troublemakerExchangeCard(String userName, int seat1, int seat2){
        //TODO check player card is troublemaker
        if(troublemakerExchangeSeat1 == null && troublemakerExchangeSeat2 == null){
            logger.debug("troublemakerExchangeCard(userName={}, seat1={}, seat2={})", userName, seat1, seat2);
            troublemakerExchangeSeat1 = seat1;
            troublemakerExchangeSeat2 = seat2;
            attemptNightAction();
        }
    }

    //强盗换牌
    public void robberSnatchCard(String userName, int seat){
        if(robberSnatchSeat == null) {
            logger.debug("robberSnatchCard(userName={}, seat={})", userName, seat);
            robberSnatchSeat = seat;
            attemptNightAction();
        }
    }
    //狼人验牌
    public void singleWolfCheckDeck(String userName, int deck){
        if(singleWolfCheckDeck == null) {
            logger.debug("singleWolfCheckDeck(userName = {}, deck = {})", userName, deck);
            singleWolfCheckDeck = deck;
            attemptNightAction();
            //TODO 如果验到一张狼人牌则可以再验一张
        }
    }

    //预言家验牌
    public void seerCheckDeck(String userName, int deck1, int deck2){
        if(seerCheckDeck1 == null && seerCheckDeck2 == null) {
            logger.debug("seerCheckDeck(userName = {}, deck1 = {}, deck2 = {})", userName, deck1, deck2);
            seerCheckDeck1 = deck1;
            seerCheckDeck2 = deck2;
            attemptNightAction();
        }
    }

    //预言家验人
    public void seerCheckPlayer(String userName, int seat){
        if(seerCheckPlayer == null) {
            logger.debug("seerCheckPlayer(userName={}, seat={})", userName, seat);
            seerCheckPlayer = seat;
            attemptNightAction();
        }
    }

    //酒鬼换牌
    public void drunkExchangeCard(String userName, int deck){
        if(drunkExchangeDeck == null) {
            logger.debug("drunkExchangeCard(userName = {}, deck = {})", userName, deck);
            drunkExchangeDeck = deck;
            attemptNightAction();
        }
    }

    //猎人投票
    public void hunterVote(String userName, int seat){
        //TODO 判定该事件是否能够触发
        if(hunterVote) {
            logger.debug("hunterVote(userName = {}, seat = {})", userName, seat);
            Player player = seatPlayerMap.get(seat);
            Set<Camp> victoryCampSet = new TreeSet();
            if (player.getCard() == Card.TANNER) {
                sendMessage(new XskrMessage("皮匠获胜", null, null));
                victoryCampSet.add(Camp.TANNER);
            } else if (player.getCard() == Card.WEREWOLF_1 || player.getCard() == Card.WEREWOLF_2) {
                sendMessage(new XskrMessage("村民阵营获胜", null, null));
                victoryCampSet.add(Camp.VILLAGER);
            } else {
                sendMessage(new XskrMessage("狼人阵营获胜", null, null));
                victoryCampSet.add(Camp.WOLF);
            }
            gameFinish(victoryCampSet);
        }
    }

    public void clearVote(){
        for(Player player:players){
            player.setVoteSeat(null);
            player.setVotedCount(0);
        }
    }

    private void roomChanged(){
        //玩家数量、座位、准备状态发生了改变，通知各个客户端目前房间的状态

    }

    public void setSimpMessagingTemplate(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }
    private void sendMessage(Player player, XskrMessage message){
        String roomWebSocketQueue = "/queue/" + id;
        System.out.println(roomWebSocketQueue + '\t' + message);
        simpMessagingTemplate.convertAndSendToUser(player.getName(), roomWebSocketQueue, message);
    }

    private void sendMessage(XskrMessage message){
        String roomWebSocketTopic = "/topic/" + id;
        System.out.println(roomWebSocketTopic + "\t" + message);
        simpMessagingTemplate.convertAndSend(roomWebSocketTopic, message);
    }

    public int getID() {
        return id;
    }

    public Card[] getCards(){
        return cards;
    }

    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public void setCurrentPlayerName(String currentPlayerName) {
        this.currentPlayerName = currentPlayerName;
        this.clientAction = getLastClientAction(currentPlayerName);
    }

    private void keepKeyMessage(Player player, XskrMessage xskrMessage){
        List<XskrMessage> messages = playerMessagesMap.get(player);
        if(messages == null){
            messages = new ArrayList();
            playerMessagesMap.put(player, messages);
        }
        messages.add(xskrMessage);
        if(xskrMessage.getAction() != null) {
            playerClientActionMap.put(player, xskrMessage.getAction());
        }
    }

    public Map<Player, List<XskrMessage>> getPlayerMessagesMap() {
        return playerMessagesMap;
    }

    public List<String> getKeyMessages(String playerName){
        Player player = getPlayerByName(playerName);
        List<XskrMessage> xskrMessages = getPlayerMessagesMap().get(player);
        List<String> messages = new ArrayList();
        for(XskrMessage xskrMessage:xskrMessages){
            messages.add(xskrMessage.getMessage());
        }
        return messages;
    }

    public ClientAction getLastClientAction(String playerName){
        Player player = getPlayerByName(playerName);
        if(playerClientActionMap != null) {
            return playerClientActionMap.get(player);
        }else{
            return null;
        }
    }

    public ClientAction getClientAction() {
        return clientAction;
    }

    public void setClientAction(ClientAction clientAction) {
        this.clientAction = clientAction;
    }
}
