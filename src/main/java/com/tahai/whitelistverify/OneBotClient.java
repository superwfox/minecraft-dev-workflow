package com.tahai.whitelistverify;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class OneBotClient extends WebSocketClient {

    public OneBotClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        OneBotApi.client = this;
        org.bukkit.Bukkit.getPluginManager().getPlugin("WhiteListVerify").getLogger().info("§f OneBotWebsocket 连接成功");
    }

    @Override
    public void onMessage(String s) {
        OneBotHandler.MsgDivider(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        retry();
    }

    @Override
    public void onError(Exception e) {
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