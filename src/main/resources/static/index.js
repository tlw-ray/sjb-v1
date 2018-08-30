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
        showMessages(message.body);
    });
});

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function showMessages(message) {
    $("#messages").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $( "#stone" ).click(function() { stompClient.send(targetURL, {}, "s")});
    $( "#scissors" ).click(function() { stompClient.send(targetURL, {}, "j") });
    $( "#cloth" ).click(function() { stompClient.send(targetURL, {}, "b") });
});




