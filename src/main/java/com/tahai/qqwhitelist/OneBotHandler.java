package com.tahai.qqwhitelist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OneBotHandler {

    private OneBotHandler() {
    }

    public static void MsgDivider(String rawMsg) {
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(rawMsg).getAsJsonObject();
        } catch (Exception e) {
            return;
        }

        if (!jsonObject.has("post_type") || !jsonObject.has("message")) return;

        JsonElement msgElement = jsonObject.get("message");
        if (!msgElement.isJsonArray()) return;

        JsonArray msgArray = msgElement.getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgArray.size(); i++) {
            JsonObject segment = msgArray.get(i).getAsJsonObject();
            sb.append(parseMsg(segment));
        }
        String parsedMsg = sb.toString();

        if (!jsonObject.has("message_type")) return;
        String messageType = jsonObject.get("message_type").getAsString();

        // 仅处理群消息
        if (!"group".equals(messageType)) return;

        String groupId = String.valueOf(jsonObject.get("group_id").getAsLong());
        if (!groupId.equals(ConfigManager.GroupId)) return;

        if (!jsonObject.has("sender")) return;
        JsonObject sender = jsonObject.getAsJsonObject("sender");
        String userQQ = String.valueOf(sender.get("user_id").getAsLong());

        GroupMsgHandler.handle(userQQ, parsedMsg);
    }

    private static String parseMsg(JsonObject obj) {
        String type = obj.has("type") ? obj.get("type").getAsString() : "";
        switch (type) {
            case "text":
                return obj.getAsJsonObject("data").get("text").getAsString();
            case "face":
                return "[表情]";
            case "image":
                return "[图片]";
            case "at": {
                String nickname = obj.getAsJsonObject("data").get("name").getAsString();
                return "[@" + nickname + "]";
            }
            case "reply":
                return "[回复]";
            case "video":
                return "[视频]";
            case "record":
                return "[语音]";
            default:
                return "[未知消息]";
        }
    }
}