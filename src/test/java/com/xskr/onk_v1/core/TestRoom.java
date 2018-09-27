package com.xskr.onk_v1.core;


import org.junit.Assert;
import org.junit.Test;

public class TestRoom {

    @Test
    public void testCreateRoom(){
        int playerCount = 9;
        int cardCount = 9 + Room.TABLE_DECK_THICKNESS;
        Card[] cards = CardUtil.getCards(cardCount);
        Room room = null;
        for(int i=0;i<playerCount;i++){
            String playerName = "player" + i;
            if(i == 0 ){
                room = new Room(0, playerName);
                //房主分配卡牌
                room.setCards(cards);
            }
            //玩家进房间就坐准备
            room.join("player" + i);
            room.pickSeat(playerName, i);
            room.setReady(playerName, true);
        }

        // 游戏开始后为每个玩家发牌，并留下三张牌作为桌面上的牌垛
        for(Seat player:room.getSeats()){
            System.out.println(player.getUserName() + " card: " + player.getCard());
            Assert.assertNotNull(player.getCard());
        }
        Assert.assertEquals(Room.TABLE_DECK_THICKNESS, room.getDesktopCards().size());
        for(Card card:room.getDesktopCards().values()){
            System.out.println("deck: " + card);
            Assert.assertNotNull(card);
        }

        // 进入夜晚每个玩家行动
        Seat singleWolf = room.getSingleWolfSeat();
        if(singleWolf != null){
            room.pickDesktopCard(singleWolf.getUserName(), 0);
        }
        Seat seer = room.getSeatByInitializeCard(Card.SEER);
        if(seer != null){
            room.pickDesktopCard(seer.getUserName(), 1);
            room.pickDesktopCard(seer.getUserName(), 2);
        }
        Seat robber = room.getSeatByInitializeCard(Card.ROBBER);
        if(robber != null){
            room.pickSeat(robber.getUserName(), 5);
        }
        Seat troublemaker = room.getSeatByInitializeCard(Card.TROUBLEMAKER);
        if(troublemaker != null){
            int seat1, seat2;
            if(room.getLocation(troublemaker) == 0){
                seat1 = playerCount - 1;
                seat2 = room.getLocation(troublemaker) + 1;
            }else if(room.getLocation(troublemaker) == playerCount-1){
                seat1 = room.getLocation(troublemaker) - 1;
                seat2 = 0;
            }else{
                seat1 = room.getLocation(troublemaker) - 1;
                seat2 = room.getLocation(troublemaker) + 1;
            }
            room.pickSeat(troublemaker.getUserName(), seat1);
            room.pickSeat(troublemaker.getUserName(), seat2);

        }
        Seat drunk = room.getSeatByInitializeCard(Card.DRUNK);
        if(drunk != null){
            room.pickDesktopCard(drunk.getUserName(), 1);
        }

        //每个玩家都行动完毕后，房间自动发动夜间流程并发消息给每个玩家

        System.out.println("--------模拟投票-------");

        //判定狼玩家位置，供后面投票使用
        Seat wolf1 = room.getSeatByInitializeCard(Card.WEREWOLF_1);
        Seat wolf2 = room.getSeatByInitializeCard(Card.WEREWOLF_2);
        Seat wolf = null;
        if(wolf1 != null){
            wolf = wolf1;
        }else if(wolf2 != null){
            wolf = wolf2;
        }

        //模拟情况1: 仅有皮匠被投出: 皮匠获胜
        Seat tanner = room.getSeatByInitializeCard(Card.TANNER);
        System.out.println(tanner);
        if(tanner != null) {
            for (Seat player : room.getSeats()) {
                room.vote(player.getUserName(), room.getLocation(tanner));
            }
        }else{
            System.out.println("场面上没有皮匠，无法模拟皮匠被单独得票最高的场景");
        }
        clearVote(room);

        //模拟情况2: 猎人被投出
        Seat hunter = room.getSeatByInitializeCard(Card.HUNTER);
        if(hunter != null){
            for(Seat player:room.getSeats()){
//                player.setVoteSeat(hunter.getLocation());
                room.pickSeat(player.getUserName(), room.getLocation(hunter));
            }

            if(wolf != null) {
                //2.1 猎人投了狼: 村民阵营胜利
//                room.hunterVote(hunter.getUserName(), wolf.getLocation());
                room.pickSeat(hunter.getUserName(), room.getLocation(wolf));
            }else{
                System.out.println("场面上没有狼存在，无法模拟有狼且猎人被投出的情况。");
            }
            if(tanner != null){
                //2.2 猎人投了皮匠: 皮匠阵营胜利
                room.hunterVote(hunter.getUserName(), room.getLocation(tanner));
            }else{
                System.out.println("场面上没有皮匠存在，无法模拟有狼且猎人被投出的情况。");
            }

            for(Seat player:room.getSeats()){
                //2.3 猎人投了村民: 狼人阵营胜利
                if(Camp.isVillagerCamp(player.getCard())){
                    room.hunterVote(hunter.getUserName(), room.getLocation(player));
                    break;
                }
            }
            clearVote(room);
        }else{
            System.out.println("场面上没有猎人存在，无法模拟有狼且猎人被投出的情况。");
        }

        //模拟情况3:
        if(wolf != null){
            //3.1 有狼且狼被投出: 村民获胜
            for(Seat player:room.getSeats()){
                room.vote(player.getUserName(), room.getLocation(wolf));
            }
            clearVote(room);
            //3.2 有狼且爪牙被投出: 狼获胜
            Seat minion = room.getSeatByInitializeCard(Card.MINION);
            if(minion != null){
                for(Seat player:room.getSeats()){
                    room.vote(player.getUserName(), room.getLocation(minion));
                }
                clearVote(room);
            }else{
                System.out.println("没有爪牙的存在，不能模拟爪牙被投出的情况。");
            }
            //3.3 有狼且村民被投出: 狼获胜
            //找到一个村民阵营的玩家
            Seat normalPlayer = null;
            for(Seat player:room.getSeats()){
                if(player.getCard() != Card.WEREWOLF_2 &&
                        player.getCard() != Card.WEREWOLF_1 &&
                        player.getCard() != Card.TANNER &&
                        player.getCard() != Card.HUNTER
                ){
                    normalPlayer = player;
                }
            }
            for(Seat player:room.getSeats()){
                room.vote(player.getUserName(), room.getLocation(normalPlayer));
            }
            clearVote(room);
        }else{
            System.out.println("场面上没有狼，无法模拟狼被投票出的场景");
        }


        //模拟情况4: 场面上没有狼, 且投票平局分布，共同获胜
        if(wolf1 != null){
            wolf1.setCard(room.getDesktopCards().get(1));
        }
        if(wolf2 != null){
            wolf2.setCard(room.getDesktopCards().get(2));
        }
        for (Seat player : room.getSeats()) {
            room.vote(player.getUserName(), room.getLocation(player));
        }
    }

    private void clearVote(Room room){
        for(Seat player:room.getSeats()){
            player.setVoteSeat(null);
            player.setVotedCount(0);
        }
    }
}
