package com.tahai.qqgroupsync;

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

        // 必须是消息上报
        if (!jsonObject.has("post_type")) return;
        String postType = jsonObject.get("post_type").getAsString();
        if (!"message".equals(postType)) return;

        // 只处理群消息
        if (!jsonObject.has("message_type")) return;
        String messageType = jsonObject.get("message_type").getAsString();
        if (!"group".equals(messageType)) return;

        // 过滤配置的群号
        if (!jsonObject.has("group_id")) return;
        String groupId = String.valueOf(jsonObject.get("group_id").getAsLong());
        if (!groupId.equals(ConfigManager.GroupId)) return;

        // 提取消息文本
        if (!jsonObject.has("message")) return;
        JsonElement msgElement = jsonObject.get("message");
        if (!msgElement.isJsonArray()) return;
        JsonArray msgArray = msgElement.getAsJsonArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < msgArray.size(); i++) {
            sb.append(parseMsg(msgArray.get(i).getAsJsonObject()));
        }
        String parsedMsg = sb.toString();
        if (parsedMsg.isEmpty()) return;

        // 安全获取发送者QQ
        if (!jsonObject.has("sender")) return;
        JsonElement senderElement = jsonObject.get("sender");
        if (!senderElement.isJsonObject()) return;
        JsonObject sender = senderElement.getAsJsonObject();
        if (!sender.has("user_id")) return;
        String userQQ = String.valueOf(sender.get("user_id").getAsLong());

        // 调用群消息处理器
        new GroupMsgHandler().handle(userQQ, parsedMsg);
    }

    private static String parseMsg(JsonObject obj) {
        String type = obj.has("type") ? obj.get("type").getAsString() : "";
        switch (type) {
            case "text": {
                if (!obj.has("data")) return "[文本]";
                JsonElement data = obj.get("data");
                if (!data.isJsonObject()) return "[文本]";
                JsonObject dataObj = data.getAsJsonObject();
                if (!dataObj.has("text")) return "[文本]";
                return dataObj.get("text").getAsString();
            }
            case "at": {
                if (!obj.has("data")) return "[@某人]";
                JsonElement data = obj.get("data");
                if (!data.isJsonObject()) return "[@某人]";
                JsonObject dataObj = data.getAsJsonObject();
                if (!dataObj.has("name")) return "[@某人]";
                String nickname = dataObj.get("name").getAsString();
                return "[@" + nickname + "]";
            }
            case "face":
                return "[表情]";
            case "image":
                return "[图片]";
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