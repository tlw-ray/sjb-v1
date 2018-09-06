package com.xskr.onk_v1.core;

import com.alibaba.fastjson.JSON;
import com.xskr.onk_v1.core.action.Action;

import java.util.*;

public class Room {

    final int TABLE_DECK_THICKNESS = 3;

    // 该房间支持的所有卡牌
    private Card[] cards;

    // 进入房间的玩家，但可能还没有入座
    private Set<Player> players;
    // 发牌后剩余的桌面3张牌垛
    private Card[] tableDeck = new Card[TABLE_DECK_THICKNESS];


    // 发牌后卡牌到玩家索引
    private Map<Card, Player> cardPlayerMap;
    // 玩家就坐后座位号到玩家索引
    private Map<Integer, Player> seatPlayerMap;


    public Room(List<Card> cardList){
        cards = cardList.toArray(new Card[0]);
        int seatCount = getSeatCount();
        players = new HashSet(seatCount);

        seatPlayerMap = new HashMap(seatCount);
        cardPlayerMap = new HashMap(seatCount);

    }

    public int getSeatCount(){
        return cards.length - TABLE_DECK_THICKNESS;
    }

    /**
     * 玩家进入房间
     * @param player
     * @return True 进入房间成功， False 房间已满进入失败
     */
    public boolean in(Player player){
        if(players.contains(player)){
            //玩家已经在该房间
            return true;
        }else{
            //玩家进入该房间
            if(players.size() < getSeatCount()){
                //房间未满
                players.add(player);
                return true;
            }else{
                return false;
            }
        }
    }

    /**
     * 玩家离开房间
     * @param player
     */
    public void out(Player player){
        players.remove(player);
        //TODO 是否需要清理索引
    }

    /**
     * 玩家坐到指定位置
     * @param player 玩家
     * @param seatNumber 座位
     * @return true 成功 false 该位置已经有人无法坐下
     */
    public boolean sit(Player player, int seatNumber){
        if(seatPlayerMap.keySet().contains(seatNumber)){
            return false;
        }else{
            //TODO 检查seat number的合法性， 从1开始到玩家数量上限
            seatPlayerMap.put(seatNumber, player);
            player.setSeat(seatNumber);
            return true;
        }
    }

    /**
     * 初始化一局游戏
     */
    public void initializeGame(){

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

        //洗牌
        deck.shuffle(500);

        //为所有人发牌，并建立牌到玩家的索引
        cardPlayerMap.clear();
        for(Player player:players){
            Card card = deck.deal();
            player.setCard(card);
            player.setSwapCard(card);
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
    public void getPlayerActionMessage(){
        Map<Player, String> playerActionMessageMap = new HashMap();
        Player wolf2 = cardPlayerMap.get(Card.WEREWOLF_1);
        Player wolf1 = cardPlayerMap.get(Card.WEREWOLF_2);
        for(Map.Entry<Card, Player> entry:cardPlayerMap.entrySet()){
            Card card = entry.getKey();
            Player player = entry.getValue();
            String message = String.format("你好，您的初始身份是%s", player.getCard());
//            switch(card){
//                case DOPPELGANGER:
//                    message = "化身幽灵你好，这个身份太复杂了，暂时没有支持。玩下去一定会bug的，赶紧换身份吧。";
//                case WEREWOLF_1:
//                    message = wolf2 != null?
//                            "狼人你好，场面上是双狼！你的同伙是" + cardPlayerMap.get(Card.WEREWOLF_2).getSeat() + "号玩家.":
//                            "孤狼你好，请选择你要查看的桌面牌垛中的一张牌(1,2,3)"
//                    ;break;
//                case WEREWOLF_2:
//                    message = wolf1 != null ?
//                            "狼人你好，场面上是双狼！你的同伙是" + cardPlayerMap.get(Card.WEREWOLF_1).getSeat() + "号玩家.":
//                            "孤狼你好，请选择你要查看的桌面牌垛中的一张牌(1,2,3)"
//                    ;break;
//                case MINION:
//                    if(wolf1 != null && wolf2 != null){
//                        message = String.format("爪牙你好，你看到场上%s号玩家和%s号玩家是双狼。", wolf1.getSeat(), wolf2.getSeat());
//                    }else if(wolf1 != null){
//                        message = String.format("爪牙你好，场上狼主子只有一位是%s号玩家，请千万伺候好它。", wolf1.getSeat());
//                    }else if(wolf2 != null){
//                        message = String.format("爪牙你好，场上狼主子只有一位是%s号玩家，请千万伺候好它。", wolf2.getSeat());
//                    }else{
//                        message = "爪牙你好，场上好像没有狼，请多保重。";
//                    }
//                case MASON_1:
//                    Player mason2 = cardPlayerMap.get(Card.MASON_2);
//                    if(mason2 == null){
//                        message = "孤独的守夜人你好，场上看起来只有你自己。";
//                    }else{
//                        message = String.format("守夜人你好，你的同伙是%s号玩家", mason2.getSeat());
//                    }
//                case MASON_2:
//                    Player mason1 = cardPlayerMap.get(Card.MASON_1);
//                    if(mason1 == null){
//                        message = "孤独的守夜人你好，场上看起来只有你自己。";
//                    }else{
//                        message = String.format("守夜人你好，你的同伙是%s号玩家", mason1.getSeat());
//                    }
//                case SEER:
//                    message = "预言家你好，请选择一个座位号以确认某位玩家的身份；或者出示1-3中两个数字查看桌面牌垛中的两张牌。";
//                case ROBBER:
//                    message = "强盗你好，请选择一个座位号，与他交换身份。";
//                case TROUBLE_MAKER:
//                    message = "捣蛋鬼你好，请选择除你(%s)之外的两个座位号，交换他们的身份。";
//                case DRUNK:
//                    message = "酒鬼你好，一会你将会从桌面牌垛中获取新的身份，而你自己也不知道自己的新身份。";
//                case INSOMNIAC:
//                    message = "失眠者你好，所有行动结束后你将会能够查看自己的身份是否有被换过。";
//                case HUNTER:
//                    message = "猎人你好，暂时不支持这个身份，玩下去一定会bug的，赶紧重开吧!!!";
//                case TANNER:
//                    message = "皮匠，嘘~~只有让他们投死你，才能赢！";
//            }
//            //TODO send message to player

        }
    }

    //处理玩家夜间行动
    public void action(Player player, Action action){
        player.setAction(action);
        for(Player player1 : players){
            if(player1.getAction() == null){
                return;
            }
        }

        //所有的Player都已制定了行动，触发行动
        for(Player player2:players){
            player2.getAction().perform(player2, this);
        }

        //发布天亮后的消息
        for(Player player3:players){

        }
    }

    private Integer singleWolfCheckDeck;
    private Integer seerCheckDeck1;
    private Integer seerCheckDeck2;
    private Integer seerCheckPlayer;
    private Integer robberSnatchSeat;
    private Integer troubleMakerExchangeSeat1;
    private Integer troubleMakerExchangeSeat2;
    private Integer drunkCheckDeck;

    //捣蛋鬼换牌
    private void exchangeCard(int seat1, int seat2){
        troubleMakerExchangeSeat1 = seat1;
        troubleMakerExchangeSeat2 = seat2;
    }

    //强盗换牌
    private void snatchCard(int seat){
        robberSnatchSeat = seat;
    }

    //狼人验牌
    private void wolfCheckDeck(int dack){
        singleWolfCheckDeck = dack;
    }

    //预言家验牌
    private void seerCheckDeck(int dack1, int dack2){
        seerCheckDeck1 = dack1;
        seerCheckDeck2 = dack2;
    }

    //预言家验人
    private void seerCheckPlayer(int seat){
        seerCheckPlayer = seat;
    }

    private boolean ready(){
        //检查捣蛋鬼是否已经行动
        if(cardPlayerMap.get(Card.TROUBLE_MAKER) != null){
            //如果存在捣蛋鬼玩家
            if(troubleMakerExchangeSeat1 == null
                    || troubleMakerExchangeSeat2 == null){
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

    private ShowCard singleWolfCheckDeckCard;
    private ShowCard seerCheckDeckCard1;
    private ShowCard seerCheckDeckCard2;
    private ShowCard seerCheckPlayerCard;
    private ShowCard robberSnatchCard;

    //所有玩家均已行动完毕，处理所有流程并发布最新的信息
    public void exchangeActionPerform(){
//        DOPPELGANGER,
//        WEREWOLF_1, WEREWOLF_2, MINION,
//        MASON_1, MASON_2,
//        SEER, ROBBER, TROUBLE_MAKER,
//        DRUNK, INSOMNIAC,
//        VILLAGER_1, VILLAGER_2, VILLAGER_3,
//        HUNTER, TANNER;

        Player singleWolf = getSingleWolf();
        Player minion = cardPlayerMap.get(Card.MINION);
        Player meson1 = cardPlayerMap.get(Card.MASON_1);
        Player meson2 = cardPlayerMap.get(Card.MASON_2);
        Player seer = cardPlayerMap.get(Card.SEER);
        Player robber = cardPlayerMap.get(Card.ROBBER);
        Player troublemaker = cardPlayerMap.get(Card.TROUBLE_MAKER);
        Player drunk = cardPlayerMap.get(Card.DRUNK);
        Player insomniac = cardPlayerMap.get(Card.INSOMNIAC);
        Player hunter = cardPlayerMap.get(Card.HUNTER);
        Player tanner = cardPlayerMap.get(Card.TANNER);

        if(singleWolf != null){
            ShowCard showCard = new ShowCard(ShowCardType.DECK, singleWolfCheckDeck, tableDeck[singleWolfCheckDeck]);
            sendMessage(singleWolf, String.format("你看到桌面牌垛中第%s张牌是: %s", singleWolfCheckDeck, tableDeck[singleWolfCheckDeck].toString()));
        }else if(minion != null){
            if(singleWolf != null){

            }
        }
        if(robber != null){
            Player player = seatPlayerMap.get(robberSnatchSeat);
            player.setSwapCard(Card.ROBBER);
            robber.setSwapCard(player.getCard());
            //给强盗出示的他抢到的卡牌
            ShowCard showCard = new ShowCard(ShowCardType.PLAYER, player.getSeat(), player.getCard());
            robber.setShowCard(showCard);
        }
        if(troublemaker != null){
            Player player1 = seatPlayerMap.get(troubleMakerExchangeSeat1);
            Player player2 = seatPlayerMap.get(troubleMakerExchangeSeat2);
            player1.setSwapCard(player2.getCard());
            player2.setSwapCard(player1.getCard());
        }
        if(drunk != null){
            Card swapCard = tableDeck[drunkCheckDeck];
            tableDeck[drunkCheckDeck] = drunk.getCard();
            drunk.setSwapCard(swapCard);
        }
    }

    private void sendMessage(Player player, String message){

    }

    public void publishMorningMessage(){

    }

    //接收投票并公布获胜信息
    public void vote(Player player, Player wolf){

    }
}
