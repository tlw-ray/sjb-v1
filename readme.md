# 桌游助手

- 20180903 完成石头剪刀布
- 20180907 更新添加致谢

[TOC]

## 致谢

- 感谢余力提供一夜狼人杀这个好玩的游戏给大家一起玩，并辛辛苦苦的提供建议持续的做法官
- 感谢几位妹子，唐雯婷、王丽娜、丁思思、吴春燕有她们世界才会更美好
- 感谢几位兄弟吴友辉、于洋、陈嗥、周元广、大家一起玩的很开心
- 最终感谢我们的大领导给大家创造良好的工作环境和氛围

## 登录功能说明

- 提供账号密码登录
- 提供第三方认证登录
- 提供手机短信登录
- 提供用户注册
- 提供密码修改
- 登陆后触发登录事件，增加在线人数
- 退出或超时触发登出事件，减少在线人数

## 房间功能说明

- 可以选择创建（并加入）房间或加入房间
- 选择创建房间会进入房间配置界面
- 选择加入房间需要提示输入房间号码
- 创建房间会触发房间创建事件
- 加入房间会导致房间内的人数增加
- 离开房间会导致房间内的人数减少
- 房间内的人员如果超过5秒不响应会显示为离线状态

## 石头剪刀布

- 能够以用户名: tlw, wyh, dss, wln, yl, yy 等登录
- 密码同用户名。
- 登陆后可以几个用户一起玩石头剪刀布游戏。
- 游戏每5秒刷新，只要超过2个玩家猜拳就会发生计分，分为胜、平、负、放弃四种情况。
- 玩家猜拳可分为石头、剪刀、布三种情况，也可以不出，视为放弃。
- 系统能够根据玩家的猜拳判断输赢状态并输出。
- 系统能够统计所有玩家的历史猜拳数据和统计。

## 一夜狼人杀

微信版：
游戏流程：
1.身份设置阶段（给用户的信息）
开房选择游戏人数N。
选择游戏人数N+3张身份牌，随机给每位玩家1张身份牌，其余3张进入弃牌堆，标记为弃牌1/2/3。
所有人确认好自己身份后，进入夜晚行动阶段。
狼人：确认狼队友。
爪牙：确认狼人。
守夜：确认另外一个守夜/确认自己是唯一的守夜

2.夜晚行动阶段（用户的操作）
所有人按照自己身份输入行动内容，之后按顺序进行结算。
狼人：孤狼确认弃牌堆中的1张牌（弃牌1/2/3）。
预言：选择确认一个玩家的身份/选择确认弃牌堆中的2张牌（弃牌1/2/3）。
强盗：选择与一个玩家身份交换并确认交换来的身份/不执行操作。
捣蛋：选择除自己以外的两名玩家交换身份。
酒鬼：选择自己的身份和弃牌堆中的1张牌（弃牌1/2/3）交换。

3.讨论投票阶段
失眠：可以确认自己的最终身份。
所有玩家按顺序讨论3轮后，选择投票的玩家，所有玩家完成投票后进入结算阶段。

4.结算阶段
显示所有玩家的最终身份已经该局游戏的胜利阵营
根据投票进行结算：
如果存在狼人且狼人得票最高/并列最高，村民获胜。
如果存在狼人且没有狼人得票最高/并列最高，狼人获胜。
如果不存在狼人且所有人得票为1，共同胜利。
如果不存在狼人且有人得票最高/并列最高，共同失败。

分析获胜阵营: 狼人、村民、皮匠
按照如下顺序判定:
1. 如果玩家中没有狼人, 且每人得票数为1, 则共同获胜
2. 否则，如果有狼被投出，则村民获胜，皮匠和狼失败
3. 否则，如果皮匠被投出，(且没有狼人被投出)则皮匠获胜，村民和狼人失败
4. 否则，（如果没有狼和皮匠被投出）则狼获胜，村民和皮匠失败

## 摇骰子功能

## 手心手背

## 你画我猜

## 你演我猜

## 猜词语

## 谁是卧底

## 投票助手

## 排号助手

## 抽奖助手
