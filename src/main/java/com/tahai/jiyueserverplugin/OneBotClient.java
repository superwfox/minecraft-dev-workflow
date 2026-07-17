package com.tahai.jiyueserverplugin;

import org.bukkit.Bukkit;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OneBotClient {
    private final URI serverUri;
    private WebSocket webSocket;
    private HttpClient httpClient;

    public OneBotClient(URI serverUri) {
        this.serverUri = serverUri;
    }

    public void connect() {
        httpClient = HttpClient.newHttpClient();
        WebSocket.Builder builder = httpClient.newWebSocketBuilder();
        String token = ConfigManager.getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        CompletableFuture<WebSocket> future = builder.buildAsync(serverUri, new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                OneBotClient.this.webSocket = webSocket;
                OneBotClient.this.onOpen();
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                OneBotClient.this.onMessage(data.toString());
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                OneBotClient.this.onClose(statusCode, reason, true);
                return null;
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                OneBotClient.this.onError(new Exception(error));
            }
        });
        webSocket = future.join();
    }

    public void disconnect() {
        if (webSocket != null && !webSocket.isOutputClosed()) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnecting");
        }
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public boolean isOpen() {
        return webSocket != null && !webSocket.isOutputClosed() && !webSocket.isInputClosed();
    }

    protected void onOpen() {
        Bukkit.getLogger().info("OneBot WebSocket 连接成功");
    }

    protected void onMessage(String s) {
    }

    protected void onClose(int code, String reason, boolean remote) {
        retry();
    }

    protected void onError(Exception e) {
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