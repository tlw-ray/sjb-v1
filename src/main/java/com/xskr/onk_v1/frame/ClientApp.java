package com.xskr.onk_v1.frame;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.apache.commons.compress.utils.Charsets;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.hc.core5.http.URIScheme.HTTP;


public class ClientApp extends Application {

    private static final String baseURL = "http://127.0.0.1:8080/";
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();

    @Override
    public void start(Stage primaryStage){
        Label userLabel = new Label("用户: ");
        TextField userTextField = new TextField("dss");
        Button loginButton = new Button("登录");

        Label roomLabel = new Label("房间号: ");
        Spinner roomSpinner = new Spinner(1, Integer.MAX_VALUE, 1);
        roomSpinner.setMaxWidth(Double.MAX_VALUE);
        Button joinRoomButton = new Button("进入");
        Button leaveRoomButton = new Button("");
        CheckBox readyCheckBox = new CheckBox("准备");

        Label deckLabel = new Label("底牌: ");
        Spinner deckSpinner = new Spinner(0, 2, 0);
        deckSpinner.setMaxWidth(Double.MAX_VALUE);
        Button wolfDeckButton = new Button("狼人看底牌");
        Button drunkExchangeButton = new Button("酒鬼换底牌");

        Label playerLabel = new Label("玩家: ");
        Spinner playerSpinner = new Spinner(0, 6, 0);
        playerSpinner.setMaxWidth(Double.MAX_VALUE);
        Button robberSnatchButton = new Button("强盗换牌");
        Button seerCheckButton = new Button("预言家验身份");
        Button voteButton = new Button("投票");

        Label deck1Label = new Label("底牌1: ");
        Spinner deck1Spinner = new Spinner(0, 2, 1);
        deck1Spinner.setMaxWidth(Double.MAX_VALUE);
        Label deck2Label = new Label("底牌2: ");
        Spinner deck2Spinner = new Spinner(0, 2, 2);
        deck2Spinner.setMaxWidth(Double.MAX_VALUE);
        Button seerSeeButton = new Button("预言家看底牌");

        Label player1Label = new Label("玩家1: ");
        Spinner player1Spinner = new Spinner(0, 6, 0);
        player1Spinner.setMaxWidth(Double.MAX_VALUE);
        Label player2Label = new Label("玩家2: ");
        Spinner player2Spinner = new Spinner(0, 6, 1);
        player2Spinner.setMaxWidth(Double.MAX_VALUE);
        Button troublemakerExchangeButton = new Button("捣蛋鬼换底牌");

        Label messageLabel = new Label("消息: ");
        TextArea messageTextArea = new TextArea();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(30);
        gridPane.setPadding(new Insets(5,5,5,5));
        gridPane.getColumnConstraints().addAll(
                new ColumnConstraints(0, 50, Integer.MAX_VALUE, Priority.NEVER, HPos.RIGHT, false),
                new ColumnConstraints(0, 120, Integer.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true),
                new ColumnConstraints(0, 80, Integer.MAX_VALUE, Priority.NEVER, HPos.RIGHT, false),
                new ColumnConstraints(0, 120, Integer.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true),
                new ColumnConstraints(0, 90, Integer.MAX_VALUE, Priority.NEVER, HPos.LEFT, false)
        );
        gridPane.addRow(0, userLabel, userTextField, loginButton);
        gridPane.addRow(1, roomLabel, roomSpinner, joinRoomButton, readyCheckBox);
        gridPane.addRow(2, deckLabel, deckSpinner, wolfDeckButton, drunkExchangeButton);
        gridPane.addRow(3, playerLabel, playerSpinner, robberSnatchButton, seerCheckButton, voteButton);
        gridPane.addRow(4, deck1Label, deck1Spinner, deck2Label, deck2Spinner, seerSeeButton);
        gridPane.addRow(5, player1Label, player1Spinner, player2Label, player2Spinner, troublemakerExchangeButton);
        gridPane.addRow(6, messageLabel, messageTextArea);
        GridPane.setHalignment(loginButton, HPos.LEFT);
        GridPane.setHalignment(joinRoomButton, HPos.LEFT);
        GridPane.setHalignment(wolfDeckButton, HPos.LEFT);
        GridPane.setHalignment(robberSnatchButton, HPos.LEFT);
        GridPane.setColumnSpan(messageTextArea, 4);
        GridPane.setFillHeight(messageTextArea, true);
        GridPane.setValignment(messageLabel, VPos.TOP);
        Scene scene = new Scene(gridPane, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        loginButton.setOnAction(e -> {
            try {
                HttpPost httpPost = new HttpPost(baseURL + "login");
                List<NameValuePair> nameValuePairs = new ArrayList();
                nameValuePairs.add(new BasicNameValuePair("username", userTextField.getText()));
                nameValuePairs.add(new BasicNameValuePair("password", userTextField.getText()));
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, Charsets.UTF_8);
                httpPost.setEntity(formEntity);
                httpClient.execute(httpPost);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        joinRoomButton.setOnAction(e -> {
            get(baseURL + roomSpinner.getValue() + "/join");
        });

        readyCheckBox.setOnAction(e->{
            boolean ready = readyCheckBox.isSelected();
            get(baseURL + roomSpinner.getValue() + "/ready/" + ready);
        });

        wolfDeckButton.setOnAction(e->{
            get(baseURL + roomSpinner.getValue() + "/singleWolf/check/" + deckSpinner.getValue());
        });

        drunkExchangeButton.setOnAction(e -> {
            get(baseURL + roomSpinner.getValue() + "/drunk/exchange/" + deckSpinner.getValue());
        });

        robberSnatchButton.setOnAction(e -> {
            get(baseURL + roomSpinner.getValue() + "/robber/snatch/" + playerSpinner.getValue());
        });

        seerCheckButton.setOnAction(e->{
            get(baseURL + roomSpinner.getValue() + "/seer/check/" + playerSpinner.getValue());
        });

        voteButton.setOnAction(e->{
            get(baseURL + roomSpinner.getValue() + "/vote/" + playerSpinner.getValue());
        });

        seerCheckButton.setOnAction(e->{
            get(baseURL + roomSpinner.getValue() + "/seer/check/" + deck1Spinner.getValue() + "/" + deck2Spinner.getValue());
        });

        troublemakerExchangeButton.setOnAction(e->{
            get(baseURL + roomSpinner.getValue() + "/truoublemaker/exchange" + player1Spinner.getValue() + "/" + player2Spinner.getValue());
        });
    }

    private void get(String url){
        HttpGet httpGet = new HttpGet(url);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
            System.out.println(response.getCode() + response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
