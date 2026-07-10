package com.tahai.randomevent;

public class PrivateMsgHandler {

    public static void handle(String userQQ, String msg) {
        msg = msg.trim();
        if (msg.isEmpty()) return;

        OneBotApi.sendP(userQQ, "请使用群聊指令进行操作。");
    }
}