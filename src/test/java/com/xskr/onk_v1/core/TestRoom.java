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
        Room room = new Room(1, cards);
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
        Assert.assertEquals(Room.TABLE_DECK_THICKNESS, room.getTableDeck().size());
        for(Card card:room.getTableDeck().values()){
            System.out.println("deck: " + card);
            Assert.assertNotNull(card);
        }

        // 进入夜晚每个玩家行动
        Player singleWolf = room.getSingleWolf();
        if(singleWolf != null){
            room.singleWolfCheckDeck(singleWolf.getName(), 0);
        }
        Player seer = room.getPlayerByInitializeCard(Card.SEER);
        if(seer != null){
            room.seerCheckDeck(seer.getName(), 1, 2);
        }
        Player robber = room.getPlayerByInitializeCard(Card.ROBBER);
        if(robber != null){
            room.robberSnatchCard(robber.getName(), 5);
        }
        Player troublemaker = room.getPlayerByInitializeCard(Card.TROUBLEMAKER);
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
        Player drunk = room.getPlayerByInitializeCard(Card.DRUNK);
        if(drunk != null){
            room.drunkExchangeCard(drunk.getName(), 1);
        }

        //每个玩家都行动完毕后，房间自动发动夜间流程并发消息给每个玩家

        System.out.println("--------模拟投票-------");

        //判定狼玩家位置，供后面投票使用
        Player wolf1 = room.getPlayerByInitializeCard(Card.WEREWOLF_1);
        Player wolf2 = room.getPlayerByInitializeCard(Card.WEREWOLF_2);
        Player wolf = null;
        if(wolf1 != null){
            wolf = wolf1;
        }else if(wolf2 != null){
            wolf = wolf2;
        }

        //模拟情况1: 仅有皮匠被投出: 皮匠获胜
        Player tanner = room.getPlayerByInitializeCard(Card.TANNER);
        System.out.println(tanner);
        if(tanner != null) {
            for (Player player : room.getPlayers()) {
                room.vote(player.getName(), tanner.getSeat());
            }
        }else{
            System.out.println("场面上没有皮匠，无法模拟皮匠被单独得票最高的场景");
        }
        room.clearVote();

        //模拟情况2: 猎人被投出
        Player hunter = room.getPlayerByInitializeCard(Card.HUNTER);
        if(hunter != null){
            for(Player player:room.getPlayers()){
                player.setVoteSeat(hunter.getSeat());
            }

            if(wolf != null) {
                //2.1 猎人投了狼: 村民阵营胜利
                room.hunterVote(hunter.getName(), wolf.getSeat());
            }else{
                System.out.println("场面上没有狼存在，无法模拟有狼且猎人被投出的情况。");
            }
            if(tanner != null){
                //2.2 猎人投了皮匠: 皮匠阵营胜利
                room.hunterVote(hunter.getName(), tanner.getSeat());
            }else{
                System.out.println("场面上没有皮匠存在，无法模拟有狼且猎人被投出的情况。");
            }

            for(Player player:room.getPlayers()){
                //2.3 猎人投了村民: 狼人阵营胜利
                if(Camp.isVillagerCamp(player.getCard())){
                    room.hunterVote(hunter.getName(), player.getSeat());
                    break;
                }
            }
            room.clearVote();
        }else{
            System.out.println("场面上没有猎人存在，无法模拟有狼且猎人被投出的情况。");
        }

        //模拟情况3:
        if(wolf != null){
            //3.1 有狼且狼被投出: 村民获胜
            for(Player player:room.getPlayers()){
                room.vote(player.getName(), wolf.getSeat());
            }
            room.clearVote();
            //3.2 有狼且爪牙被投出: 狼获胜
            Player minion = room.getPlayerByInitializeCard(Card.MINION);
            if(minion != null){
                for(Player player:room.getPlayers()){
                    room.vote(player.getName(), minion.getSeat());
                }
                room.clearVote();
            }else{
                System.out.println("没有爪牙的存在，不能模拟爪牙被投出的情况。");
            }
            //3.3 有狼且村民被投出: 狼获胜
            //找到一个村民阵营的玩家
            Player normalPlayer = null;
            for(Player player:room.getPlayers()){
                if(player.getCard() != Card.WEREWOLF_2 &&
                        player.getCard() != Card.WEREWOLF_1 &&
                        player.getCard() != Card.TANNER &&
                        player.getCard() != Card.HUNTER
                ){
                    normalPlayer = player;
                }
            }
            for(Player player:room.getPlayers()){
                room.vote(player.getName(), normalPlayer.getSeat());
            }
            room.clearVote();
        }else{
            System.out.println("场面上没有狼，无法模拟狼被投票出的场景");
        }


        //模拟情况4: 场面上没有狼, 且投票平局分布，共同获胜
        if(wolf1 != null){
            wolf1.setCard(room.getTableDeck().get(1));
        }
        if(wolf2 != null){
            wolf2.setCard(room.getTableDeck().get(2));
        }
        for (Player player : room.getPlayers()) {
            room.vote(player.getName(), player.getSeat());
        }
    }
}
