package com.tahai.qqgroupsync;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class OneBotClient extends WebSocketClient {

    public OneBotClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        OneBotApi.client = this;
        Plugin plugin = Bukkit.getPluginManager().getPlugin("QQGroupSync");
        if (plugin != null) {
            plugin.getLogger().info("OneBotWebsocket 连接成功");
        }
    }

    @Override
    public void onMessage(String message) {
        OneBotHandler.MsgDivider(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        retry();
    }

    @Override
    public void onError(Exception ex) {
        // 保持静默
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