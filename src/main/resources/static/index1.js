var endpointURL = '/onk/endpoint'
var topicSock;
var topicStomp;
$(document).ready(function () {
    //订阅个人频道
    var queueURL = '/user/queue'
    var queueSock = new SockJS(endpointURL)
    var queueStomp = Stomp.over(queueSock)
    queueStomp.connect({}, function (frame) {
        //记录当前客户端玩家身份
        console.log(frame)
        $('#player_hidden').val(frame['headers']['user-name'])
        queueStomp.subscribe(queueURL, function (message) {
            var xskrMessage = JSON.parse(message.body)
            //将收到消息的message显示
            if (xskrMessage.message != null) {
                $('#messages_div').append('<div>' + xskrMessage.message + '</div>')
            }
            //处理个人频道推送的信息
            var clientAction = xskrMessage.action
            if(clientAction == 'RECONNECT'){
                //断线重连
                //除了刷新房间信息，还需要输出该玩家的关键消息
                var room = xskrMessage.data
                var playerName = $('#player_hidden').val()
                // refreshPlayerList(room)
                for(var i in room.seats){
                    var seat = room.seats[i]
                    if(seat['userName'] == playerName){
                        var keyMessages = seat['keyMessages']
                        //显示本局该玩家所有关键游戏信息
                        var lastXskrMessage
                        for(var j in keyMessages){
                            lastXskrMessage = keyMessages[j]
                            $('#messagesView').append(lastXskrMessage.message + '<br>')
                            //最后一次关键消息的ClientAction
                            clientAction = lastXskrMessage.action
                        }
                        //断线重连根据最后一次clientAction决定行动按钮的具体行为
                        // clientAction = lastXskrMessage.action
                        break
                    }
                }
            }
            if(clientAction == 'LEAVE_ROOM'){
                //实现离开房间的逻辑
                $('#create_join_div').show()
                $('#room_div').hide()
                $('#room_id_number').prop('disabled', false)
                //断开监听房间共有频道
                closeTopic()
            }else if (clientAction == 'SINGLE_WOLF_ACTION') {
                enableDesktopCards()
                enablePlayersWithoutSelf()
            } else if (clientAction == 'DRUNK_ACTION') {
                enableDesktopCards()
            } else if (clientAction == 'ROBBER_ACTION') {
                enablePlayersWithoutSelf()
            } else if (clientAction == 'SEER_ACTION') {
                enableDesktopCards()
                enablePlayersWithoutSelf()
            } else if (clientAction == 'TROUBLEMAKER_ACTION') {
                enablePlayersWithoutSelf()
            } else if (clientAction == 'VOTE_ACTION') {
                enablePlayersWithoutSelf()
            } else if (clientAction == 'HUNTER_VOTE_ACTION') {
                enablePlayersWithoutSelf()
            } else {
                console.log("Unsupported action: " + clientAction)
            }
        })
    })

    //确定创建新房间
    $('#create_room_button').click(function () {
        $.ajax({
            url: '/onk/create',
            type: 'POST',
            Accept: "application/json",
            contentType: "application/json",
            success: function (roomID) {
                //更新创建的房间ID, 以便于后面用到房间参数时候可以从这里获取
                $('#room_id_number').val(roomID)
                subscribeRoom(true)
            },
            error: function (ex) {
                console.log(ex)
            }
        })
    })

    //确定加入已有房间
    $('#apply_join_button').click(function () {
        subscribeRoom(true)
    })

    $('#config_card_div > :checkbox').each(function(idx, element){
        $(element).bind('click', {}, function(e){
            var selectedCards = $("#config_card_div input:checked").map(function () {
                return this.value
            }).get()
            var roomID = $('#room_id_number').val()
            $.ajax({
                url: '/onk/cards/' + roomID,
                type: 'POST',
                data: JSON.stringify(selectedCards),
                Accept: "application/json",
                contentType: "application/json",
                success: function () {
                },
                error: function (ex) {
                    console.log(ex)
                }
            })
        })
    })

    //准备，开始游戏
    $('#ready_checkbox').click(function () {
        //清空之前的获胜信息
        $('.outcome').html('')
        //清空消息栏
        $('#messages_div').html('')
        ajaxGet('/onk/ready/' + $('#room_id_span').html() + '/' + $('#ready_checkbox').prop('checked'))
    })

    $('#exit_button').click(function(){
        var roomID = $('#room_id_number').val()
        ajaxGet('/onk/leave/' + roomID)
        $('#create_join_div').show()
        $('#room_div').hide()
        $('#messages_div').html('')
    })

    $('#deck_div > button').click(function(){
        var url = '/onk/card/' + $('#room_id_number').val() + '/' + this.value
        ajaxGet(url)
    })

    $('#seat_div > .seat').click(function(e){
        //点击空白座位
        e.cancelBubble = true
        e.stopPropagation()     //阻止事件向上传递
        e.preventDefault()      //放弃默认的行为，例如双击选中
        var roomID = $('#room_id_span').html()
        var url = '/onk/seat/' + roomID + '/' + $(this).attr('location')
        ajaxGet(url)
    })
    function subscribeRoom(joinRoomAfterSubscribe) {
        var roomID = $('#room_id_number').val()
        //如果已经已经有订阅，那么先退订
        closeTopic()

        //订阅该房间的公共频道
        var topicURL = '/topic/' + roomID
        topicSock = new SockJS(endpointURL)
        topicStomp = Stomp.over(topicSock)
        topicStomp.connect('guest', 'guest', function (frame) {
            topicStomp.subscribe(topicURL, function (message) {
                var xskrMessage = JSON.parse(message.body)
                // disableAll()
                var clientAction = xskrMessage.action
                if (clientAction == 'ROOM_CHANGED') {
                    //刷新房间信息
                    var room = xskrMessage.data
                    refreshPlayerList(room)
                }else if (clientAction == 'GAME_FINISH') {
                    //重置ready供玩家开启新一局游戏
                    $('#ready_checkbox').prop('checked', false)
                    $('#ready_checkbox').prop('disabled', false)
                    //输出玩家输赢状态
                    for (var idx in xskrMessage.data) {
                        var data = xskrMessage.data[idx]
                        var outcomeMessage = getCampName(data.camp)
                        if (data.win) {
                            outcomeMessage += ' 获胜 '
                        } else {
                            outcomeMessage += ' 失败 '
                        }
                        outcomeMessage = outcomeMessage + '(' + getCardName(data.initializeCard) + '->' + getCardName(data.finalCard) + ')'
                        $('#player_outcome_span_' + idx).html(outcomeMessage)
                    }
                } else if (clientAction == 'VOTE_ACTION') {
                    enablePlayersWithoutSelf()
                } else if(clientAction == 'NEW_GAME'){
                    $('#ready_checkbox').prop('disabled', true)
                }
                if (xskrMessage.message != null) {
                    $('#messages_div').append('<div>' + xskrMessage.message + '</div>')
                }
            })
            console.log('Topic subscribed...')
            if(joinRoomAfterSubscribe){
                ajaxGet('/onk/join/' + roomID)
            }
        })
    }
})

function closeTopic(){
    if(topicStomp != null){
        topicStomp.disconnect(function(){
            console.log('Topic stomp disconnected...')
        })
    }
    if(topicSock != null){
        topicSock.close()
    }
}

function disableAll(){
    $('input').prop('disabled', true)
    $('button').prop('disabled', true)
    //玩家的DIV不能再点击
    // $('input').prop('onclick', null).off('click')
    // $('div').prop('onclick', null).off('click')
}

function enableDesktopCards(){
    $('#deck_div > button').prop('disabled', false)
}

function enablePlayersWithoutSelf(){
    $('#seat_div > * > button').prop('disabled', false)
    //不能点自己
    var playerName = $('#player_hidden').val()
    // alert($('#seat_div > div[playerName="' + playerName + '"]').length)
    $('#seat_div > div[playerName="' + playerName + '"]').prop('disabled', true)
}

function refreshPlayerList(room) {
    $('#room_id_span').html(room.id)
    $('#create_join_div').hide()
    $('#room_div').show()

    //刷新房间卡牌信息
    $('#config_card_div > input').prop('checked', false)
    for (var i in room.cards) {
        var card = room.cards[i]
        var checkboxName = '#' + card.toLowerCase() + '_checkbox'
        $(checkboxName).prop('checked', true)
    }

    //将玩家放置到座位区域上去
    for (var idx in room.seats) {
        var seat = room.seats[idx]
        var seatDiv = $('#seat_' + idx)
        seatDiv.html("")
        if(idx >= 0 && idx < room.availableSeatCount){
            //当房主改变房间设定时，可能会出现玩家座位超出可用座位数的情况, 这里保障座位是有效的
            var playerName = seat['userName']
            if(playerName != null){
                var ready = seat['ready']
                var playerInfo = idx + '. ' + playerName;
                var playerButtonID = 'player_button_' + idx;
                if (playerName == $('#player_hidden').val()) {
                    //标记当前玩家
                    playerInfo = '☺' + playerInfo
                    //刷新当前玩家的准备状态
                    $('#ready_checkbox').prop('checked', ready)

                    //如果玩家是房主且没有准备则可以在准备期间修改房间的卡牌设定
                    if(playerName == room.owner && !ready){
                        $('#config_card_div > :checkbox').prop('disabled', false)
                    }else{
                        $('#config_card_div > :checkbox').prop('disabled', true)
                    }
                } else {
                    //do nothing
                }

                if(playerName == room.owner){
                    playerInfo = '_ ' + playerInfo
                }else{
                    //do nothing
                }

                if(ready){
                    playerInfo = playerInfo + ' √'
                }else{
                    //do nothing
                }

                var playerButton = $('<button/>', {
                    'id': playerButtonID,
                    'text': playerInfo
                })

                var outcome = $('<span/>', {
                    'class': 'outcome',
                    'id': 'player_outcome_span_' + idx
                })
                seatDiv.append(playerButton)
                seatDiv.append(outcome)
                seatDiv.attr('playerName', playerName)
            }
            seatDiv.css('background-color', 'gray')
        }else{
            //不可用的座位
            seatDiv.css('background-color', 'lightgray')
        }
    }
}

function ajaxGet(url) {
    console.log('call: ' + url)
    $.ajax({
        url: url,
        type: 'GET',
        Accept: "application/json",
        contentType: "application/json",
        success: function (data) {
            return data
        },
        error: function (ex) {
            console.log(ex)
        }
    });
}

//获得阵营中文名称
function getCampName(camp) {
    if (camp == 'TANNER') {
        return '皮匠'
    } else if (camp == 'WOLF') {
        return '狼人'
    } else if (camp == 'VILLAGER') {
        return '村民'
    } else {
        return camp
    }
}

//获得卡牌中文名称
function getCardName(card) {
    if (card == 'WEREWOLF_1') {
        return '狼人'
    } else if (card == 'WEREWOLF_2') {
        return '狼人'
    } else if (card == 'MINION') {
        return '爪牙'
    } else if (card == 'MASON_1') {
        return '守夜人'
    } else if (card == 'MASON_2') {
        return '守夜人'
    } else if (card == 'ROBBER') {
        return '强盗'
    } else if (card == 'SEER') {
        return '预言家'
    } else if (card == 'TANNER') {
        return '皮匠'
    } else if (card == 'TROUBLEMAKER') {
        return '捣蛋鬼'
    } else if (card == 'VILLAGER_1') {
        return '村民'
    } else if (card == 'VILLAGER_2') {
        return '村民'
    } else if (card == 'VILLAGER_3') {
        return '村民'
    } else if (card == 'DRUNK') {
        return '酒鬼'
    } else if (card == 'HUNTER') {
        return '猎人'
    } else if (card == 'INSOMNIAC') {
        return '失眠者'
    } else {
        return card
    }
}
