package com.xskr.onk_v1.core;

import com.alibaba.fastjson.JSON;
import com.xskr.common.XskrMessage;
import com.xskr.onk_v1.ONK_WebSocketMessageBrokerConfigurer;
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
    // 发牌后卡牌到玩家索引
    private Map<Card, Player> cardPlayerMap;
    // 玩家就坐后座位号到玩家索引
    private Map<Integer, Player> seatPlayerMap;

    // 玩家夜间操作
    private Integer singleWolfCheckDeck;
    private Integer seerCheckDeck1;
    private Integer seerCheckDeck2;
    private Integer seerCheckPlayer;
    private Integer robberSnatchSeat;
    private Integer troublemakerExchangeSeat1;
    private Integer troublemakerExchangeSeat2;
    private Integer drunkExchangeDeck;

    //用于发送WebSocket信息
    private SimpMessagingTemplate simpMessagingTemplate;

    public Room(int id, List<Card> cardList){
        logger.debug("Room(cardList = {})", Arrays.toString(cardList.toArray()));
        this.id = id;
        this.cards = cardList.toArray(new Card[0]);
        int seatCount = getSeatCount();
        this.players = new TreeSet();

        this.seatPlayerMap = new HashMap(seatCount);
        this.cardPlayerMap = new HashMap(seatCount);
        this.namePlayerMap = new HashMap(seatCount);
    }

    public int getSeatCount(){
        return cards.length - TABLE_DECK_THICKNESS;
    }

    /**
     * 玩家进入房间
     * @param playerName
     * @return 返回玩家座位
     */
    public TreeMap<Integer, String> join(String playerName){
        logger.debug("join(playerName = {})", playerName);
        Player player = namePlayerMap.get(playerName);
        if(player != null){
            //玩家已经在该房间

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
                        String messageContent = String.format("%s加入房间。", player.getName());
                        XskrMessage xskrMessage = new XskrMessage(messageContent, player);
                        sendMessage(xskrMessage);
                        break;
                    }
                }
            }else{
                throw new RuntimeException("房间已满");
            }
        }
        TreeMap<Integer, String> seatPlayerNameMap = new TreeMap();
        for(Map.Entry<Integer, Player> entry:seatPlayerMap.entrySet()){
            seatPlayerNameMap.put(entry.getKey(), entry.getValue().getName());
        }
        return seatPlayerNameMap;
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
            cardPlayerMap.remove(player.getCard());
            seatPlayerMap.remove(player.getSeat());
        }
    }

    /**
     * 玩家坐到指定位置, 当所有玩家都准备好时游戏自动开始
     * TODO 或许应该加个倒数5,4,3,2,1
     * @param playerName 玩家
     * @return true 成功 false 该位置已经有人无法坐下
     */
    public void setReady(String playerName, boolean ready){
        logger.debug("setReady(playerName = {}, ready = {})", playerName, ready);
        Player player = namePlayerMap.get(playerName);
        if(player != null) {
            player.setReady(ready);
        }else{
            String message = String.format("玩家%s不在房间，无法设定准备状态。", playerName);
            throw new RuntimeException(message);
        }
        for(Player player1:players){
            if(!player1.isReady()){
                return;
            }
        }
        newGame();
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

    public Player getPlayerByCard(Card card){
        return cardPlayerMap.get(card);
    }

    public Player getPlayerBySeat(int seat){
        return seatPlayerMap.get(seat);
    }
    /**
     * 初始化一局游戏
     */
    private void newGame(){
        logger.debug("newGame()");

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
        cardPlayerMap.clear();
        for(Player player:players){
            Card card = deck.deal();
            player.setCard(card);
            cardPlayerMap.put(card, player);
        }

        //建立桌面剩余的牌垛
        tableDeck.put(1, deck.deal());
        tableDeck.put(2, deck.deal());
        tableDeck.put(3, deck.deal());

        logger.debug("playerAction");
        //需要主动行动的玩家
        Player singleWolf = getSingleWolf();
        Player seer = cardPlayerMap.get(Card.SEER);
        Player robber = cardPlayerMap.get(Card.ROBBER);
        Player troublemaker = cardPlayerMap.get(Card.TROUBLEMAKER);
        Player drunk = cardPlayerMap.get(Card.DRUNK);

        //发牌结束后根据身份为每个玩家发送行动提示信息
        String playerSeatRange = "1--" + players.size();
        for(Map.Entry<Card, Player> entry:cardPlayerMap.entrySet()){
            Player player = entry.getValue();
            if(player != null) {
                String message;
                if(player == singleWolf){
                    message = String.format("请输入1-3中的一个数字，来查看牌垛中对应的纸牌");
                }else if (player == seer) {
                    message = String.format("请输入1-3中的两个数字来查看的牌垛中对应的纸牌，或者输入所有玩家(%s)的座位号之一，逗号分隔。", 1, playerSeatRange);
                }else if(player == robber){
                    message = String.format("请输入所有玩家(%s)的座位号之一，查看并交换该身份。", playerSeatRange);
                }else if(player == troublemaker){
                    message = String.format("请输入除您(%s)之外两个玩家(%s)的座位号，交换他们的身份。", player.getSeat(), playerSeatRange);
                }else if(player == drunk){
                    message = String.format("请输入牌垛中三张纸牌1-3中的一个号码，与之交换身份卡。");
                }else{
                    message = "所有玩家行动完成后，系统会给出关于您身份的进一步提示信息。";
                }
                //向玩家发送提示信息
                XskrMessage xskrMessage1 = new XskrMessage(String.format("您的初始身份是%s。", player.getCard().getDisplayName()), null);
                XskrMessage xskrMessage2 = new XskrMessage(message, null);
                sendMessage(player, xskrMessage1);
                sendMessage(player, xskrMessage2);
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
        if(cardPlayerMap.get(Card.SEER) != null){
            //如果存在预言家角色
            if((seerCheckDeck1 == null || seerCheckDeck2 == null) &&
                    seerCheckPlayer == null){
                //如果语言家既没有指定要看牌垛中的那两张牌，也没有指定要验证的玩家的身份
                logger.debug("seer not work!");
                return false;
            }
        }

        // 检查强盗是否已经行动
        if(cardPlayerMap.get(Card.ROBBER) != null){
            //如果存在强盗玩家
            if(robberSnatchSeat == null){
                // 如果强盗没有指定要交换的位置
                logger.debug("robber not work!");
                return false;
            }
        }

        //检查捣蛋鬼是否已经行动
        if(cardPlayerMap.get(Card.TROUBLEMAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troublemakerExchangeSeat1 == null
                    || troublemakerExchangeSeat2 == null){
                //如果捣蛋鬼没有指定要交换的位置
                logger.debug("troublemaker not work!");
                return false;
            }
        }

        //检查酒鬼是否行动
        if(cardPlayerMap.get(Card.DRUNK) != null){
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
        Player wolf1 = cardPlayerMap.get(Card.WEREWOLF_1);
        Player wolf2 = cardPlayerMap.get(Card.WEREWOLF_2);
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
//          Player doopelganger = cardPlayerMap.get(Card.DOPPELGANGER);
            Player singleWolf = getSingleWolf();
            Player wolf1 = cardPlayerMap.get(Card.WEREWOLF_1);
            Player wolf2 = cardPlayerMap.get(Card.WEREWOLF_2);
            Player minion = cardPlayerMap.get(Card.MINION);
            Player meson1 = cardPlayerMap.get(Card.MASON_1);
            Player meson2 = cardPlayerMap.get(Card.MASON_2);
            Player seer = cardPlayerMap.get(Card.SEER);
            Player robber = cardPlayerMap.get(Card.ROBBER);
            Player troublemaker = cardPlayerMap.get(Card.TROUBLEMAKER);
            Player drunk = cardPlayerMap.get(Card.DRUNK);
            Player insomniac = cardPlayerMap.get(Card.INSOMNIAC);

            //狼的回合
            if(singleWolf != null){
                //场面上是一头孤狼
                XskrMessage message = new XskrMessage(String.format("看到桌面牌垛中第%s张牌是: %s", singleWolfCheckDeck, tableDeck.get(singleWolfCheckDeck).getDisplayName()), null);
                sendMessage(singleWolf, message);
            }else if(wolf1 != null && wolf2 != null){
                //有两个狼玩家
                String messageTemplate = "看到狼人伙伴%s号玩家'%s'看向你，露出了狡黠的目光。";
                XskrMessage wolf1Message = new XskrMessage(String.format(messageTemplate, wolf2.getSeat(), wolf2.getName()), null);
                XskrMessage wolf2Message = new XskrMessage(String.format(messageTemplate, wolf1.getSeat(), wolf1.getName()), null);
                sendMessage(wolf1, wolf1Message);
                sendMessage(wolf2, wolf2Message);
            }else if(wolf1 == null && wolf2 == null){
                //场面上没有狼，不需要给任何狼发消息
            }

            // 爪牙的回合
            if(minion != null){
                String message;
                if(wolf1 != null && wolf2 != null){
                    //双狼
                    message = String.format("哇竟然有两个狼主子，看到了%s号玩家'%s'和%s号玩家'%s'举起了手。",
                            wolf1.getSeat(), wolf1.getName(), wolf2.getSeat(), wolf2.getName());
                }else if(singleWolf != null){
                    //孤狼
                    message = String.format("看到了一头孤狼，%s号玩家'%s'确实是心仪已久的主子。",
                            singleWolf.getSeat(), singleWolf.getName());
                }else{
                    //无狼
                    message = "天哪，竟然一条狼也没有。";
                }
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(minion, xskrMessage);
            }

            // 守夜人的回合
            if(meson1 == null && meson2 != null){
                //单守夜
                sendMessage(meson2, new XskrMessage("发现竟然只有自己站在漆黑的夜里。", null));
            }else if(meson1 != null && meson2 == null){
                sendMessage(meson1, new XskrMessage("发现竟然只有自己站在漆黑的夜里。", null));
            }else if(meson1 != null && meson2 != null){
                //双守夜
                String messageTemplate = "看到另一位守夜人，%s号玩家'%s'正目光炯炯有神的望着你。";
                XskrMessage meson1Message = new XskrMessage(String.format(messageTemplate, meson2.getSeat(), meson2.getName()), null);
                XskrMessage meson2Message = new XskrMessage(String.format(messageTemplate, meson1.getSeat(), meson1.getName()), null);
                sendMessage(meson1, meson1Message);
                sendMessage(meson2, meson2Message);
            }else{
                //无守夜
            }

            //预言家回合
            if(seer != null){
                //查看一位玩家
                String message = "";
                if(seerCheckPlayer != null){
                    Player player = seatPlayerMap.get(seerCheckPlayer);
                    message = String.format("小心翼翼的翻开了%s号玩家'%s'的身份，看到他的真实身份是: %s",
                            player.getSeat(), player.getName(), player.getCard().getDisplayName());
                }else{
                    Card card1 = tableDeck.get(seerCheckDeck1);
                    Card card2 = tableDeck.get(seerCheckDeck2);
                    message = String.format("翻开桌上第%s和%s张卡牌，看到了身份卡%s和%s安静的躺在那里。", seerCheckDeck1, seerCheckDeck2, card1, card2);
                }
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(seer, xskrMessage);
            }

            if(robber != null){
                Player player = seatPlayerMap.get(robberSnatchSeat);
                Card swapCard = player.getCard();
                player.setCard(robber.getCard());
                robber.setCard(swapCard);
                //更新缓存
                cardPlayerMap.put(player.getCard(), player);
                cardPlayerMap.put(robber.getCard(), robber);
                String message = String.format("粗暴的抢夺了%s号玩家'%s'的身份牌，并将自己的身份塞给了他，冷静下来看到上面赫然写着%s。",
                        player.getSeat(), player.getName(), swapCard);
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(robber, xskrMessage);
            }
            if(troublemaker != null){
                Player player1 = seatPlayerMap.get(troublemakerExchangeSeat1);
                Player player2 = seatPlayerMap.get(troublemakerExchangeSeat2);
                Card swapCard = player1.getCard();
                player1.setCard(player2.getCard());
                player2.setCard(swapCard);
                //更新缓存
                cardPlayerMap.put(player1.getCard(), player1);
                cardPlayerMap.put(player2.getCard(), player2);
                String message = String.format("成功交换了%s号玩家'%s'和%s号玩家'%s'的身份牌，他们发现了一定会暴跳如雷，嘿嘿嘿。",
                        player1.getSeat(), player1.getName(), player2.getSeat(), player2.getName());
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(troublemaker, xskrMessage);
            }
            if(drunk != null){
                Card swapCard = tableDeck.get(drunkExchangeDeck);
                tableDeck.put(drunkExchangeDeck, drunk.getCard());
                drunk.setCard(swapCard);
                //更新缓存
                cardPlayerMap.put(drunk.getCard(), drunk);
                cardPlayerMap.remove(swapCard, drunk);
                String message = String.format("喝的醉醺醺，随手把身份卡插入牌垛里的第%s张，并把它原有的那张抽了出来带在身上，还没来得及看就呼呼睡着了。", drunkExchangeDeck);
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(drunk, xskrMessage);
            }
            if(insomniac != null){
                String message;
                if(insomniac.getCard() == Card.INSOMNIAC){
                    message = "睡不着觉又看身份牌，它确实还是失眠者... -_-!";
                }else{
                    message = String.format("睡不着觉又看身份牌，它竟然变成了%s。难道，刚才真的睡着了？", insomniac.getCard().getDisplayName());
                }
                XskrMessage xskrMessage = new XskrMessage(message, null);
                sendMessage(insomniac, xskrMessage);
            }
            sendMessage(new XskrMessage("进行三轮讨论后请投票。", null));
        }
    }

    //接受玩家投票，计算，并公布获胜信息
    public void vote(String playerName, int seat){
        logger.debug("vote(playerName = {}, seat= {})", playerName, seat);
        Player player = namePlayerMap.get(playerName);
        player.setVoteSeat(seat);
        //票数在finishGame时统计，这里仅做投票
        if(canStatVote()){
            finishGame();
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
        //1. 如果获得最大投票数的玩家中包含猎人身份，，则由猎人获得当前投票结果后独立投票另一位玩家
        //1. 否则，如果只有皮匠获得最大票数，则皮匠阵营获胜
        //2. 否则，
        //3. 否则，如果获得最大票数的角色中有狼，则村民获胜，皮匠和狼失败; 否则狼获胜，村民和皮匠失败

        // 统计并广播获胜阵营信息，游戏结束
        Set<Camp> victoryCamp = new TreeSet();
        Set<Camp> defeatCamp = new TreeSet();

        if(voteStat.onlyVotedTanner()){
            //如果只有皮匠被投出则皮匠阵营获胜
            victoryCamp.add(Camp.TANNER);
            defeatCamp.add(Camp.WOLF);
            defeatCamp.add(Camp.VILLAGER);
        }else if (voteStat.hasWolfInPlayers()) {
            // 如果所有玩家中有狼
            if(voteStat.voted(Card.HUNTER)){
                // 如果有猎人被投中，则触发猎人技能

                // 广播当前投票信息
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
                sendMessage(new XskrMessage(report.toString(), null));
                //提示猎人由他独立投票
                Player hunter = cardPlayerMap.get(Card.HUNTER);
                sendMessage(hunter, new XskrMessage("请投票", null));
                return ;
            }else if(voteStat.voted(Card.WEREWOLF_1) || voteStat.voted(Card.WEREWOLF_2)){
                victoryCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.TANNER);
                defeatCamp.add(Camp.WOLF);
            }else{
                victoryCamp.add(Camp.WOLF);
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.TANNER);
            }
        }else{
            //如果没有狼
            if (voteStat.getMaxVoteCount() == 1) {
                //共同获胜
                victoryCamp.add(Camp.TANNER);
                victoryCamp.add(Camp.VILLAGER);
                victoryCamp.add(Camp.WOLF);
            } else if(voteStat.onlyVotedTanner()){
                //皮匠获胜
                victoryCamp.add(Camp.TANNER);
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }else{
                //共同失败
                defeatCamp.add(Camp.TANNER);
                defeatCamp.add(Camp.VILLAGER);
                defeatCamp.add(Camp.WOLF);
            }
        }
        gameFinish(victoryCamp, defeatCamp);
    }

    private void gameFinish(Set<Camp> victoryCamp, Set<Camp> defeatCamp) {
        //生成获胜信息报告
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("\n");
        for(Camp camp:victoryCamp){
            reportBuilder.append("\t获胜阵营: ");
            reportBuilder.append(camp);
            reportBuilder.append("\n");
            Set<Card> victoryCardSet = Camp.getCards(camp);
            appendPlayer(reportBuilder, victoryCardSet);
        }
        for(Camp camp:defeatCamp){
            reportBuilder.append("\t失败阵营: ");
            reportBuilder.append(camp);
            reportBuilder.append("\n");
            Set<Card> defeatCardSet = Camp.getCards(camp);
            appendPlayer(reportBuilder, defeatCardSet);
        }


        //广播消息
        sendMessage(new XskrMessage(reportBuilder.toString(), null));

        //解除所有玩家的准备状态，本局游戏结束
        for(Player player:players){
            player.setReady(false);
        }
        //TODO 通知所有客户端
    }

    private void appendPlayer(StringBuilder reportBuilder, Set<Card> victoryCardSet) {
        for (Player player : players) {
            Card card = player.getCard();
            if (victoryCardSet.contains(card)) {
                reportBuilder.append("\t\t");
                reportBuilder.append(player);
                reportBuilder.append("\n");
            }
        }
    }

    //捣蛋鬼换牌
    public void troublemakerExchangeCard(String userName, int seat1, int seat2){
        //TODO check player card is troublemaker
        logger.debug("troublemakerExchangeCard(userName={}, seat1={}, seat2={})", userName, seat1, seat2);
        troublemakerExchangeSeat1 = seat1;
        troublemakerExchangeSeat2 = seat2;
        attemptNightAction();
    }

    //强盗换牌
    public void robberSnatchCard(String userName, int seat){
        logger.debug("robberSnatchCard(userName={}, seat={})", userName, seat);
        robberSnatchSeat = seat;
        attemptNightAction();
    }

    //狼人验牌
    public void singleWolfCheckDeck(String userName, int deck){
        logger.debug("singleWolfCheckDeck(userName = {}, deck = {})", userName, deck);
        singleWolfCheckDeck = deck;
        attemptNightAction();
    }

    //预言家验牌
    public void seerCheckDeck(String userName, int deck1, int deck2){
        logger.debug("seerCheckDeck(userName = {}, deck1 = {}, deck2 = {})", userName, deck1, deck2);
        seerCheckDeck1 = deck1;
        seerCheckDeck2 = deck2;
        attemptNightAction();
    }

    //预言家验人
    public void seerCheckPlayer(String userName, int seat){
        logger.debug("seerCheckPlayer(userName={}, seat={})", userName, seat);
        seerCheckPlayer = seat;
        attemptNightAction();
    }

    //酒鬼换牌
    public void drunkExchangeCard(String userName, int deck){
        logger.debug("drunkExchangeCard(userName = {}, deck = {})", userName, deck);
        drunkExchangeDeck = deck;
        attemptNightAction();
    }

    //猎人投票
    public void hunterVote(String userName, int seat){
        logger.debug("hunterVote(userName = {}, seat = {})", userName, seat);
        Player player = seatPlayerMap.get(seat);
        Set<Camp> victoryCampSet = new TreeSet();
        Set<Camp> defeatCampSet = new TreeSet();
        if(player.getCard() == Card.TANNER){
            sendMessage(new XskrMessage("皮匠获胜", null));
            victoryCampSet.add(Camp.TANNER);
            defeatCampSet.add(Camp.WOLF);
            defeatCampSet.add(Camp.VILLAGER);
        }else if(player.getCard() == Card.WEREWOLF_1 || player.getCard() == Card.WEREWOLF_2){
            sendMessage(new XskrMessage("村民阵营获胜", null));
            victoryCampSet.add(Camp.VILLAGER);
            defeatCampSet.add(Camp.WOLF);
            defeatCampSet.add(Camp.TANNER);
        }else{
            sendMessage(new XskrMessage("狼人阵营获胜", null));
            victoryCampSet.add(Camp.WOLF);
            defeatCampSet.add(Camp.VILLAGER);
            defeatCampSet.add(Camp.TANNER);
        }
        gameFinish(victoryCampSet, defeatCampSet);
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
        String send = String.format("To %s: %s", player.getName(), message);
        String router = ONK_WebSocketMessageBrokerConfigurer.ONK_PUBLIC + "/" + player.getName();
        System.out.println(router + send);
        simpMessagingTemplate.convertAndSend(router, message);
    }

    private void sendMessage(XskrMessage message){
        String router = ONK_WebSocketMessageBrokerConfigurer.ONK_PUBLIC;
        System.out.println(router + "\t" + message);
        simpMessagingTemplate.convertAndSend(router, message);
    }

    public int getID() {
        return id;
    }

    public Card[] getCards(){
        return cards;
    }
}
