package com.tahai.randomevent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class OneBotApi {

    public static OneBotClient client;

    public static void sendG(String message) {
        if (client == null || !client.isOpen()) return;

        JsonObject params = new JsonObject();
        params.addProperty("group_id", ConfigManager.GroupId);
        params.addProperty("message", stripColor(message));
        params.addProperty("auto_escape", false);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_group_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendP(String userQQ, String message) {
        if (client == null || !client.isOpen()) return;

        JsonObject params = new JsonObject();
        params.addProperty("user_id", userQQ);
        params.addProperty("message", stripColor(message));
        params.addProperty("auto_escape", false);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_private_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendGroupAt(String userQQ) {
        if (client == null || !client.isOpen()) return;

        JsonObject atData = new JsonObject();
        atData.addProperty("qq", userQQ);

        JsonObject atSegment = new JsonObject();
        atSegment.addProperty("type", "at");
        atSegment.add("data", atData);

        JsonArray msgArray = new JsonArray();
        msgArray.add(atSegment);

        JsonObject params = new JsonObject();
        params.addProperty("group_id", ConfigManager.GroupId);
        params.add("message", msgArray);

        JsonObject request = new JsonObject();
        request.addProperty("action", "send_group_msg");
        request.add("params", params);

        try {
            client.send(request.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String stripColor(String s) {
        return s == null ? "" : s.replaceAll("§[0-9a-fk-or]", "");
    }
}