package com.xskr.onk_v1.core;


import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class TestRoom {

    @Test
    public void testCreateRoom(){
        int playerCount = 9;
        int cardCount = 9 + Room.TABLE_DECK_THICKNESS;
        List<Card> cards = CardUtil.getCards(cardCount);
        Room room = new Room(cards);
        for(int i=0;i<playerCount;i++){
            room.join("player" + i);
        }

        // 所有玩家进入准备状态则自然开始游戏
        Set<Player> players = room.getPlayers();
        Assert.assertEquals(playerCount, players.size());
        for(Player player:players){
            room.setReady(player.getName(), true);
        }

        // 游戏开始后为每个玩家发牌，并留下三张牌作为桌面上的牌垛
        for(Player player:room.getPlayers()){
            System.out.println(player.getName() + " card: " + player.getCard());
            Assert.assertNotNull(player.getCard());
        }
        Assert.assertEquals(Room.TABLE_DECK_THICKNESS, room.getTableDeck().length);
        for(Card card:room.getTableDeck()){
            System.out.println("deck: " + card);
            Assert.assertNotNull(card);
        }

        // 进入夜晚每个玩家行动
        Player singleWolf = room.getSingleWolf();
        if(singleWolf != null){
            room.singleWolfCheckDeck(singleWolf.getName(), 0);
        }
        Player seer = room.getPlayerByCard(Card.SEER);
        if(seer != null){
            room.seerCheckDeck(seer.getName(), 1, 2);
        }
        Player robber = room.getPlayerByCard(Card.ROBBER);
        if(robber != null){
            room.robberSnatchCard(robber.getName(), 5);
        }
        Player troublemaker = room.getPlayerByCard(Card.TROUBLEMAKER);
        if(troublemaker != null){
            int seat1, seat2;
            if(troublemaker.getSeat() == 0){
                seat1 = playerCount - 1;
                seat2 = troublemaker.getSeat() + 1;
            }else if(troublemaker.getSeat() == playerCount-1){
                seat1 = troublemaker.getSeat() - 1;
                seat2 = 0;
            }else{
                seat1 = troublemaker.getSeat() - 1;
                seat2 = troublemaker.getSeat() + 1;
            }
            room.troublemakerExchangeCard(troublemaker.getName(), seat1, seat2);
        }
        Player drunk = room.getPlayerByCard(Card.DRUNK);
        if(drunk != null){
            room.drunkExchangeCard(drunk.getName(), 1);
        }

        //每个玩家都行动完毕后，房间自动发动夜间流程并发消息给每个玩家

        //模拟情况1: 仅有皮匠被投出: 皮匠获胜
        Player tanner = room.getPlayerByCard(Card.TANNER);
        for(Player player: room.getPlayers()){
            room.vote(player.getName(), tanner.getSeat());
        }

        //模拟情况2: 没有狼被投出: 狼人获胜
        Player minion = room.getPlayerByCard(Card.MINION);
        for(Player player:room.getPlayers()){
            room.vote(player.getName(), minion.getSeat());
        }
        //模拟情况3: 有狼被投出: 村民获胜
        Player wolf1 = room.getPlayerByCard(Card.WEREWOLF_1);
        Player wolf2 = room.getPlayerByCard(Card.WEREWOLF_2);
        Player wolf = null;
        if(wolf1 != null){
            wolf = wolf1;
        }else if(wolf2 != null){
            wolf = wolf2;
        }
        if(wolf != null){
            for(Player player:room.getPlayers()){
                room.vote(player.getName(), wolf.getSeat());
            }
        }

        //模拟情况4: 场面上没有狼, 投票平局分布，共同获胜
        for(Player player:room.getPlayers()){
            room.vote(player.getName(), player.getSeat());
        }
    }
}
