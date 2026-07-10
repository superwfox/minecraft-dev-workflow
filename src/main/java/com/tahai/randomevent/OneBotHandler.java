package com.tahai.randomevent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class OneBotHandler {

    public static void MsgDivider(String rawMsg) {
        JsonObject jsonObject;
        try {
            jsonObject = JsonParser.parseString(rawMsg).getAsJsonObject();
        } catch (Exception e) {
            return;
        }

        if (!jsonObject.has("post_type")) return;
        if (!jsonObject.has("message")) return;

        JsonElement msgElement = jsonObject.get("message");
        if (!msgElement.isJsonArray()) return;

        JsonArray msgArray = msgElement.getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgArray.size(); i++) {
            sb.append(parseMsg(msgArray.get(i).getAsJsonObject()));
        }
        String parsedMsg = sb.toString();

        if (!jsonObject.has("message_type")) return;
        String messageType = jsonObject.get("message_type").getAsString();

        switch (messageType) {
            case "private" -> {
                JsonObject sender = jsonObject.getAsJsonObject("sender");
                String userQQ = String.valueOf(sender.get("user_id").getAsLong());
                PrivateMsgHandler.handle(userQQ, parsedMsg);
            }
            case "group" -> {
                String groupId = String.valueOf(jsonObject.get("group_id").getAsLong());
                if (!groupId.equals(ConfigManager.GroupId)) return;

                JsonObject sender = jsonObject.getAsJsonObject("sender");
                String userQQ = String.valueOf(sender.get("user_id").getAsLong());
                GroupMsgHandler.handle(userQQ, parsedMsg);
            }
        }
    }

    private static String parseMsg(JsonObject obj) {
        StringBuilder mb = new StringBuilder();
        String type = obj.has("type") ? obj.get("type").getAsString() : "";
        switch (type) {
            case "text"   -> mb.append(obj.getAsJsonObject("data").get("text").getAsString());
            case "face"   -> mb.append("[表情]");
            case "image"  -> mb.append("[图片]");
            case "at" -> {
                String nickname = obj.getAsJsonObject("data").get("name").getAsString();
                mb.append("[@").append(nickname).append("]");
            }
            case "reply"  -> mb.append("[回复]");
            case "video"  -> mb.append("[视频]");
            case "record" -> mb.append("[语音]");
            default       -> mb.append("[未知消息]");
        }
        return mb.toString();
    }
}