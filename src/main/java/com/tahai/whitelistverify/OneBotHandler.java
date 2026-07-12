package com.tahai.whitelistverify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OneBotHandler {

    public static void handleMessage(String json) {
        JsonObject root;
        try {
            root = JsonParser.parseString(json).getAsJsonObject();
        } catch (Exception e) {
            return;
        }

        if (!root.has("post_type") || !root.has("message")) return;
        if (!"message".equals(root.get("post_type").getAsString())) return;

        String messageType = root.has("message_type") ? root.get("message_type").getAsString() : "";
        if (!"group".equals(messageType)) return;

        String groupId = root.has("group_id") ? String.valueOf(root.get("group_id").getAsLong()) : "";
        if (!groupId.equals(ConfigManager.GroupId)) return;

        JsonObject sender = root.getAsJsonObject("sender");
        String userQQ = String.valueOf(sender.get("user_id").getAsLong());

        JsonElement msgElement = root.get("message");
        if (!msgElement.isJsonArray()) return;
        JsonArray msgArray = msgElement.getAsJsonArray();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgArray.size(); i++) {
            sb.append(parseMsg(msgArray.get(i).getAsJsonObject()));
        }
        String parsedMsg = sb.toString();

        new GroupMsgHandler().handle(userQQ, parsedMsg);
    }

    public static void MsgDivider(String rawMsg) {
        handleMessage(rawMsg);
    }

    private static String parseMsg(JsonObject obj) {
        String type = obj.has("type") ? obj.get("type").getAsString() : "";
        switch (type) {
            case "text"   -> { return obj.getAsJsonObject("data").get("text").getAsString(); }
            case "face"   -> { return "[表情]"; }
            case "image"  -> { return "[图片]"; }
            case "at"     -> { return "[@" + obj.getAsJsonObject("data").get("name").getAsString() + "]"; }
            case "reply"  -> { return "[回复]"; }
            case "video"  -> { return "[视频]"; }
            case "record" -> { return "[语音]"; }
            default       -> { return "[未知消息]"; }
        }
    }
}