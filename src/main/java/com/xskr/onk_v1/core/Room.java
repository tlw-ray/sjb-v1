package com.xskr.onk_v1.core;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Room {

    private Logger logger = LoggerFactory.getLogger(getClass());

    final int TABLE_DECK_THICKNESS = 3;

    // 该房间支持的所有卡牌
    private Card[] cards;

    // 进入房间的玩家，但可能还没有入座
    private Set<Player> players;
    // 发牌后剩余的桌面3张牌垛
    private Card[] tableDeck = new Card[TABLE_DECK_THICKNESS];


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
    private Integer drunkCheckDeck;

    public Room(List<Card> cardList){
        logger.debug(Arrays.toString(cardList.toArray()));
        cards = cardList.toArray(new Card[0]);
        int seatCount = getSeatCount();
        players = new TreeSet();

        seatPlayerMap = new HashMap(seatCount);
        cardPlayerMap = new HashMap(seatCount);
        namePlayerMap = new HashMap(seatCount);
    }

    public int getSeatCount(){
        return cards.length - TABLE_DECK_THICKNESS;
    }

    /**
     * 玩家进入房间
     * @param playerName
     * @return 返回玩家座位
     */
    public int join(String playerName){
        logger.debug(playerName);
        Player player = namePlayerMap.get(playerName);
        if(player != null){
            //玩家已经在该房间
            return player.getSeat();
        }else{
            player = new Player(playerName);
            //玩家进入该房间
            if(players.size() < getSeatCount()){
                //房间未满
                players.add(player);
                //为玩家分配一个座位, 从1到最大座位数遍历，发现一个座位没有人便分配
                for(int i=1;i<=getSeatCount();i++){
                    if(seatPlayerMap.get(i) == null){
                        seatPlayerMap.put(i, player);
                        namePlayerMap.put(playerName, player);
                        player.setSeat(i);
                        return i;
                    }
                }
            }else{
                throw new RuntimeException("房间已满");
            }
        }
        return -1; //TODO 未考虑到的座位分配逻辑
    }

    /**
     * 玩家离开房间
     * @param playerName
     */
    public void leave(String playerName){
        logger.debug(playerName);
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
     * 玩家坐到指定位置
     * @param playerName 玩家
     * @return true 成功 false 该位置已经有人无法坐下
     */
    public boolean setReady(String playerName, boolean ready){
        logger.debug("playerName = {}, ready = {}", playerName, ready);
        Player player = namePlayerMap.get(playerName);
        if(player != null) {
            player.setReady(ready);
            return ready;
        }else{
            String message = String.format("玩家%s不在房间，无法设定准备状态。", playerName);
            throw new RuntimeException(message);
        }
    }

    public boolean sit(String playerName, int seat){
        logger.debug("playerName = {}, seat = {}", playerName, seat);
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

    /**
     * 初始化一局游戏
     */
    public void newGame(){
        logger.debug("");

        Deck deck = new Deck(cards);

        //检查玩家数量与预期数量一致
        if(players.size() != getSeatCount()){
            throw new RuntimeException(String.format("玩家数量%s与座位数量%s不符!!", players.size(), getSeatCount()));
        }

        //检查玩家是否都已经就坐, 座位号是否符合逻辑
        for(int i=0;i<getSeatCount();i++){
            int seatNumber = i + 1;
            Player player = seatPlayerMap.get(seatNumber);
            if(player != null) {
                if (player.getSeat() != seatNumber) {
                    throw new RuntimeException(String.format("玩家%s的座位号不符合逻辑.", JSON.toJSONString(player)));
                }
            }else{
                throw new RuntimeException(String.format("玩家%s尚未就坐.", JSON.toJSONString(player)));
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
        drunkCheckDeck = null;
        //清空上一局所有玩家的身份与投票状态
        for(Player player:players){
            player.setCard(null);
            player.setVoteSeat(null);
            player.setVotedCount(0);
        }

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
        tableDeck[0] = deck.deal();
        tableDeck[1] = deck.deal();
        tableDeck[2] = deck.deal();
    }

    /**
     * 发牌结束后根据身份为每个玩家发送行动提示信息
     * @return
     */
    public void afterDealCard(){
        logger.debug("");
        //需要主动行动的玩家
        Player seer = cardPlayerMap.get(Card.SEER);
        Player robber = cardPlayerMap.get(Card.ROBBER);
        Player troublemaker = cardPlayerMap.get(Card.TROUBLEMAKER);
        Player drunk = cardPlayerMap.get(Card.DRUNK);

        String greet = "你好，您的初始身份是%s。";
        String playerSeatRange = "1--" + players.size();
        for(Map.Entry<Card, Player> entry:cardPlayerMap.entrySet()){
            Player player = entry.getValue();
            if(player != null) {
                String message;
                if (player == seer) {
                    message = String.format("请输入要查看的牌垛中纸牌1-3中的两个号码，或者输入所有玩家(%s)的座位号之一，逗号分隔。", 1, playerSeatRange);
                }else if(player == robber){
                    message = String.format("请输入所有玩家(%s)的座位号之一，查看并交换该身份。");
                }else if(player == troublemaker){
                    message = String.format("请输入除您(%s)之外两个玩家(%s)的座位号，交换他们的身份。", player.getSeat(), playerSeatRange);
                }else if(player == drunk){
                    message = String.format("请输入牌垛中三张纸牌1-3中的一个号码，与之交换身份卡。");
                }else{
                    message = "所有玩家行动完成后，系统会给出关于您身份的进一步提示信息。";
                }
                //向玩家发送提示信息
                sendMessage(player, String.format(greet, player));
                sendMessage(player, message);
            }
        }
    }

    //是否能够进入白天，当所有需要操作的玩家行动完毕才能进入白天
    private boolean canBright(){
        logger.debug("");
        //检查捣蛋鬼是否已经行动
        if(cardPlayerMap.get(Card.TROUBLEMAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troublemakerExchangeSeat1 == null
                    || troublemakerExchangeSeat2 == null){
                //如果捣蛋鬼没有指定要交换的位置
                return false;
            }
        }

        // 检查强盗是否已经行动
        if(cardPlayerMap.get(Card.ROBBER) != null){
            //如果存在强盗玩家
            if(robberSnatchSeat == null){
                // 如果强盗没有指定要交换的位置
                return false;
            }
        }

        // 检查孤狼是否已经行动
        if(getSingleWolf() != null){
            //如果存在孤狼的角色
            if(singleWolfCheckDeck == null){
                //如果孤狼没有指定要看的牌垛中的一张牌
                return false;
            }
        }

        //检查预言家是否已经行动
        if(cardPlayerMap.get(Card.SEER) != null){
            //如果存在预言家角色
            if((seerCheckDeck1 == null || seerCheckDeck2 == null) &&
                    seerCheckPlayer == null){
                //如果语言家既没有指定要看牌垛中的那两张牌，也没有指定要验证的玩家的身份
                return false;
            }
        }
        //通过了所有的检查
        return true;
    }

    //获得孤狼玩家
    private Player getSingleWolf(){
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

    //夜间行动: 所有玩家均已声明行动完毕，开始真正处理玩家们的行动，处理所有流程并发布最新的信息
    public void nightAction(){
        logger.debug("");
//        Player doopelganger = cardPlayerMap.get(Card.DOPPELGANGER);
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
//        Player hunter = cardPlayerMap.get(Card.HUNTER);
//        Player tanner = cardPlayerMap.get(Card.TANNER);

        //狼的回合
        if(singleWolf != null){
            //场面上是一头孤狼
            sendMessage(singleWolf, String.format("你看到桌面牌垛中第%s张牌是: %s", singleWolfCheckDeck, tableDeck[singleWolfCheckDeck].toString()));
        }else if(wolf1 != null && wolf2 != null){
            //有两个狼玩家
            String messageTemplate = "你看到%s号玩家看向你，露出了狡黠的目光。对了他就是你的狼人伙伴，在此同时他也看到了你。";
            sendMessage(wolf1, String.format(messageTemplate, wolf2));
            sendMessage(wolf2, String.format(messageTemplate, wolf1));
        }else if(wolf1 == null && wolf2 == null){
            //场面上没有狼，不需要给任何狼发消息
        }

        // 爪牙的回合
        if(minion != null){
            String message;
            if(wolf1 != null && wolf2 != null){
                //双狼
                message = String.format("哇竟然有两个狼主子，你看到了%s和%s号玩家举起了手。", wolf1, wolf2);
            }else if(singleWolf != null){
                //孤狼
                message = String.format("看到了一头孤狼，%s号玩家确实是你心仪已久的主子。", singleWolf);
            }else{
                //无狼
                message = "天哪，竟然一条狼也没有。";
            }
            sendMessage(minion, message);
        }

        // 守夜人的回合
        if(meson1 == null && meson2 != null){
            //单守夜
            sendMessage(meson2, "发现竟然只有自己站在漆黑的夜里。");
        }else if(meson1 != null && meson2 == null){
            sendMessage(meson1, "发现竟然只有自己站在漆黑的夜里。");
        }else if(meson1 != null && meson2 != null){
            //双守夜
            String messageTemplate = "看到另一位守夜人，%s号玩家正目光炯炯有神的望着你。";
            sendMessage(meson1, String.format(messageTemplate, meson2));
            sendMessage(meson2, String.format(messageTemplate, meson1));
        }else{
            //无守夜
        }

        //预言家回合
        if(seer != null){
            //查看一位玩家
            String message = "";
            if(seerCheckPlayer != null){
                Player player = seatPlayerMap.get(seerCheckPlayer);
                message = String.format("你小心翼翼的翻开了%s号玩家的身份，看到他的真实身份是: %s", seerCheckPlayer, player.getCard());
            }else{
                Card card1 = tableDeck[seerCheckDeck1];
                Card card2 = tableDeck[seerCheckDeck2];
                message = String.format("翻开桌上第%s和%s张卡牌，你看到了身份卡%s和%s安静的躺在那里", seerCheckDeck1, seerCheckDeck2, card1, card2);
            }
            sendMessage(seer, message);
        }

        if(robber != null){
            Player player = seatPlayerMap.get(robberSnatchSeat);
            Card swapCard = player.getCard();
            player.setCard(robber.getCard());
            robber.setCard(swapCard);
            String message = String.format("你粗暴的抢夺了%s号玩家的身份牌，并将自己的身份塞给了他，冷静下来看到上面赫然写着%s。", robberSnatchSeat, swapCard);
            sendMessage(robber, message);
        }
        if(troublemaker != null){
            Player player1 = seatPlayerMap.get(troublemakerExchangeSeat1);
            Player player2 = seatPlayerMap.get(troublemakerExchangeSeat2);
            Card swapCard = player1.getCard();
            player1.setCard(player2.getCard());
            player2.setCard(swapCard);
            String message = String.format("你成功交换了%s和%s玩家的身份牌，他们发现了一定会暴跳如雷，嘿嘿嘿。", player1, player2);
            sendMessage(troublemaker, message);
        }
        if(drunk != null){
            Card swapCard = tableDeck[drunkCheckDeck];
            tableDeck[drunkCheckDeck] = drunk.getCard();
            drunk.setCard(swapCard);
            String message = String.format("喝的醉醺醺的你随手把身份卡插入牌垛里的第%s张，并把它原有的那张抽了出来带在身上，还没来得及看就呼呼睡着了。");
            sendMessage(drunk, message);
        }
        if(insomniac != null){
            String message;
            if(insomniac.getCard() == Card.INSOMNIAC){
                message = "睡不着觉又把身份牌拿出看了一遍，它确实还是失眠者... -_-!";
            }else{
                message = "睡不着觉又把身份牌拿出看了一遍，它竟然奇迹般的变成了%s。难道，刚才真的睡着了？";
            }
            sendMessage(insomniac, message);
        }
    }

    private void sendMessage(Player player, String message){
        //TODO 发送消息到客户端
    }

    //接受玩家投票，计算，并公布获胜信息
    public void vote(String playerName, int seat){
        logger.debug("playerName={}, seat={}", playerName, seat);
        Player player = namePlayerMap.get(playerName);
        player.setVoteSeat(seat);
        if(checkVoteFinished()){
            finishGame();
        }
    }

    private boolean checkVoteFinished(){
        for(Player player: players){
            if(player.getVoteSeat() == null){
                return false;
            }
        }
        return true;
    }

    private void finishGame(){
        logger.debug("");
        //根据玩家的投票情况对每个玩家进行票数统计
        //并判断玩家队伍中是否存在狼
        boolean hasWolf = false;
        for(Player player: players){
            int voteSet = player.getVoteSeat();
            Player votedPlayer = seatPlayerMap.get(voteSet);
            votedPlayer.beVote();
            if(player.getCard() == Card.WEREWOLF_1 || player.getCard() == Card.WEREWOLF_2){
                hasWolf = true;
            }
        }

        //找到被投票最多的票数
        int maxVote = 0;
        for(Player player:players){
            if(player.getVotedCount() > maxVote){
                maxVote = player.getVotedCount();
            }
        }

        //找到该投票次数的玩家
        Set<Player> votedPlayer = new HashSet();
        for(Player player:players){
            if(player.getVotedCount() == maxVote){
                votedPlayer.add(player);
            }
        }

        boolean hasVotedWolf = false;
        boolean hasVotedTanner = false;
        boolean hasVotedVillager = false;
        for(Player player:votedPlayer){
            Card card = player.getCard();
            if(Camp.isTannerCamp(card)){
                hasVotedTanner = true;
            }else if(Camp.isWolfCamp(card)){
                hasVotedWolf = true;
            }else{
                hasVotedVillager = true;
            }
        }

        //分析获胜阵营: 狼人、村民、皮匠
        //按照如下顺序判定:
        //1. 如果只有皮匠被投出，则皮匠阵营获胜，其余阵营失败
        //2. 否则，如果玩家中没有狼人, 且每人得票数为1, 则共同获胜
        //2. 否则，如果有狼被投出，则村民获胜，皮匠和狼失败
        //4. 否则，（没有狼和皮匠被投出）则狼获胜，村民和皮匠失败
        Set<Camp> victoryCamp = new TreeSet();
        Set<Camp> defeatCamp = new TreeSet();
        if(hasVotedTanner && !hasVotedVillager && !hasVotedWolf){
            //如果只有皮匠被投出则皮匠阵营获胜
            victoryCamp.add(Camp.TANNER);
            defeatCamp.add(Camp.WOLF);
            defeatCamp.add(Camp.VILLAGER);
        }
        if(!hasWolf && maxVote == 1){
            //共同获胜
            victoryCamp.add(Camp.TANNER);
            victoryCamp.add(Camp.VILLAGER);
            victoryCamp.add(Camp.WOLF);
        }else if(hasVotedWolf){
            victoryCamp.add(Camp.VILLAGER);
            defeatCamp.add(Camp.TANNER);
            defeatCamp.add(Camp.WOLF);
        }else{
            victoryCamp.add(Camp.WOLF);
            defeatCamp.add(Camp.VILLAGER);
            defeatCamp.add(Camp.TANNER);
        }

        //生成获胜信息报告
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("获胜阵营: ");
        reportBuilder.append(Arrays.toString(victoryCamp.toArray()));
        reportBuilder.append("\n");
        reportBuilder.append("失败阵营: ");
        reportBuilder.append(Arrays.toString(defeatCamp.toArray()));
        reportBuilder.append("\n");
        for(Camp camp:victoryCamp){
            reportBuilder.append(camp);
            reportBuilder.append("\n");
            Set<Card> victoryCardSet = Camp.getCards(camp);
            for(Player player:players){
                Card card = player.getCard();
                if(victoryCardSet.contains(card)){
                    reportBuilder.append("\t");
                    reportBuilder.append(player);
                }
            }
        }

        //广播消息
        for(Player player:players){
            sendMessage(player, reportBuilder.toString());
        }
    }

    //捣蛋鬼换牌
    public void troublemakerExchangeCard(String userName, int seat1, int seat2){
        //TODO check player card is troublemaker
        logger.debug("userName={}, seat1={}, seat2={}", userName, seat1, seat2);
        troublemakerExchangeSeat1 = seat1;
        troublemakerExchangeSeat2 = seat2;
    }

    //强盗换牌
    public void robberSnatchCard(String userName, int seat){
        logger.debug("userName={}, seat={}", userName, seat);
        robberSnatchSeat = seat;
    }

    //狼人验牌
    public void singleWolfCheckDeck(String userName, int deck){
        logger.debug("userName = {}, deck = {}", userName, deck);
        singleWolfCheckDeck = deck;
    }

    //预言家验牌
    public void seerCheckDeck(String userName, int deck1, int deck2){
        logger.debug("userName = {}, deck1 = {}, deck2 = {}", userName, deck1, deck2);
        seerCheckDeck1 = deck1;
        seerCheckDeck2 = deck2;
    }

    //预言家验人
    public void seerCheckPlayer(String userName, int seat){
        logger.debug("userName={}, seat={}", userName, seat);
        seerCheckPlayer = seat;
    }

    //酒鬼换牌
    public void drunkExchangeCard(String userName, int deck){
        logger.debug("userName = {}, deck = {}", userName, deck);
        drunkCheckDeck = deck;
    }
}
