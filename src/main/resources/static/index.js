var stompClient = null;
var endpointURL = "/endpoint";
var topicURL = "/topic";
var targetURL = "/app/req";
var socket = new SockJS(endpointURL)
stompClient = Stomp.over(socket)
stompClient.onerror = function(e){console.log(e)}
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe(topicURL, function (message) {
        console.log(message.body)
        var playerDataPacks = JSON.parse(message.body)
        // var report = "";
        // for(var playerName in playerDataPacks){
        //
        // }
        // showMessages(report);
        showHistory(playerDataPacks)
    });
});

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function showMessages(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>")
}

function showHistory(playerDataPacks){
    //将内容展现到历史信息表
    var message = "<tr><td>"
    var history = ""
    for(var playerName in playerDataPacks){
        var dataPack = playerDataPacks[playerName]
        history += "<tr><td>"
        history += playerName;
        history += "</td><td>"
        history += dataPack["fingerCount"]["ROCK"]
        history += "</td><td>"
        history += dataPack["fingerCount"]["SCISSORS"]
        history += "</td><td>"
        history += dataPack["fingerCount"]["PAPER"]
        history += "</td><td>"
        history += dataPack["endsCount"]["VICTORY"]
        history += "</td><td>"
        history += dataPack["endsCount"]["TIE"]
        history += "</td><td>"
        history += dataPack["endsCount"]["DEFEAT"]
        history += "</td><td>"
        history += dataPack["endsCount"]["GIVE_UP"]
        history += "</td></tr>
        console.log(playerName + ": " + dataPack)
        var finger = dataPack["finger"]
        var ends = dataPack["ends"]
        message += (playerName + ", " + finger + ", " + ends + "<br>")
    }
    message += "</td></tr>"

    // 输出到历史表
    $("#history_tbody").html(history);
    // 输出到消息表
    $("#messages").append(message)
}

$(function () {
    $( "#stone" ).click(function() { stompClient.send(targetURL, {}, "ROCK")});
    $( "#scissors" ).click(function() { stompClient.send(targetURL, {}, "SCISSORS") });
    $( "#cloth" ).click(function() { stompClient.send(targetURL, {}, "PAPER") });
});




