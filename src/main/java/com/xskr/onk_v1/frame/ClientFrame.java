package com.xskr.onk_v1.frame;

import javax.swing.*;
import java.awt.*;

public class ClientFrame extends JFrame {
    public static void main(String[] args){
        ClientFrame clientFrame = new ClientFrame();
        clientFrame.setVisible(true);
    }

    public ClientFrame(){
        JLabel userLabel = new JLabel("用户名: ");
        JTextField userTextField = new JTextField();
        JLabel passwordLabel = new JLabel("密码: ");
        JTextField passwordTextField = new JTextField();
        JButton loginButton = new JButton("登录");

        JLabel roomLabel = new JLabel("房间号: ");
        JSpinner roomSpinner = new JSpinner();
        JButton joinRoomButton = new JButton("进入");
        JCheckBox readyCheckBox = new JCheckBox("准备");

        JLabel deckLabel = new JLabel("底牌: ");
        JSpinner deckSpinner = new JSpinner();
        JButton wolfDeckButton = new JButton("狼人看底牌");
        JButton drunkExchangeButton = new JButton("酒鬼换底牌");

        JLabel playerLabel = new JLabel("玩家: ");
        JSpinner playerSpinner = new JSpinner();
        JButton robberSnatchButton = new JButton("强盗换牌");
        JButton seerCheckButton = new JButton("预言家验身份");
        JButton voteButton = new JButton("投票");

        JLabel deck1Label = new JLabel("底牌1: ");
        JSpinner deck1Spinner = new JSpinner();
        JLabel deck2Label = new JLabel("底牌2: ");
        JSpinner deck2Spinner = new JSpinner();
        JButton seerSeeButton = new JButton("预言家看底牌");

        JLabel player1Label = new JLabel("玩家1: ");
        JSpinner player1Spinner = new JSpinner();
        JLabel player2Label = new JLabel("玩家2: ");
        JSpinner player2Spinner = new JSpinner();
        JButton troublemakerExchangeButton = new JButton("捣蛋鬼换底牌");

        GridBagLayout gridBagLayout = new GridBagLayout();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }
}
