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
import org.apache.hc.client5.http.fluent.Form;
import org.apache.hc.client5.http.fluent.Request;

import java.io.IOException;


public class ClientApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Label userLabel = new Label("用户: ");
        TextField userTextField = new TextField();
        Button loginButton = new Button("登录");

        Label roomLabel = new Label("房间号: ");
        Spinner roomSpinner = new Spinner();
        roomSpinner.setMaxWidth(Double.MAX_VALUE);
        Button joinRoomButton = new Button("进入");
        CheckBox readyCheckBox = new CheckBox("准备");

        Label deckLabel = new Label("底牌: ");
        Spinner deckSpinner = new Spinner();
        deckSpinner.setMaxWidth(Double.MAX_VALUE);
        Button wolfDeckButton = new Button("狼人看底牌");
        Button drunkExchangeButton = new Button("酒鬼换底牌");

        Label playerLabel = new Label("玩家: ");
        Spinner playerSpinner = new Spinner();
        playerSpinner.setMaxWidth(Double.MAX_VALUE);
        Button robberSnatchButton = new Button("强盗换牌");
        Button seerCheckButton = new Button("预言家验身份");
        Button voteButton = new Button("投票");

        Label deck1Label = new Label("底牌1: ");
        Spinner deck1Spinner = new Spinner();
        deck1Spinner.setMaxWidth(Double.MAX_VALUE);
        Label deck2Label = new Label("底牌2: ");
        Spinner deck2Spinner = new Spinner();
        deck2Spinner.setMaxWidth(Double.MAX_VALUE);
        Button seerSeeButton = new Button("预言家看底牌");

        Label player1Label = new Label("玩家1: ");
        Spinner player1Spinner = new Spinner();
        player1Spinner.setMaxWidth(Double.MAX_VALUE);
        Label player2Label = new Label("玩家2: ");
        Spinner player2Spinner = new Spinner();
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
            System.out.println(e);
            try {
                Request.Post("http://127.0.0.1:8080/login")
                        .bodyForm(Form.form().add("username",  userTextField.getText()).add("password",  userTextField.getText()).build())
                        .execute().returnContent();
                Request.Get("http://127.0.0.1:8080").execute();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }
}
