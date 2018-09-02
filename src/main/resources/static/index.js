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
        var playerDataPack = JSON.parse(message.body)
        var report = "";
        for(var playerName in playerDataPack){
            var dataPack = playerDataPack[playerName]
            console.log(playerName + ": " + playerDataPack[playerName])
            var finger = dataPack["finger"];
            var ends = dataPack["ends"];
            report += (playerName + ", " + finger + ", " + ends + "<br>")
        }
        showMessages(report);
    });
});

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function showMessages(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $( "#stone" ).click(function() { stompClient.send(targetURL, {}, "ROCK")});
    $( "#scissors" ).click(function() { stompClient.send(targetURL, {}, "SCISSORS") });
    $( "#cloth" ).click(function() { stompClient.send(targetURL, {}, "PAPER") });
});




