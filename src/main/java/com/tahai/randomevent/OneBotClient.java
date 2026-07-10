package com.tahai.randomevent;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class OneBotClient extends WebSocketClient {

    private final OneBotHandler handler;

    public OneBotClient(URI serverUri) {
        super(serverUri);
        this.handler = new OneBotHandler();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        OneBotApi.client = this;
        Bukkit.getLogger().info(ChatColor.WHITE + "OneBotWebsocket 连接成功");
    }

    @Override
    public void onMessage(String s) {
        handler.MsgDivider(s);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        retry();
    }

    @Override
    public void onError(Exception e) {
        // 保持静默，避免刷屏
    }

    private void retry() {
        if (this.isOpen()) return;
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            this.reconnect();
        }, "OneBot-Reconnect").start();
    }
}