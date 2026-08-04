package com.tahai.authwebmanager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebServerManager {

    private static final long SESSION_TIMEOUT = 30 * 60 * 1000L;

    private final AuthMeIntegration authMe = new AuthMeIntegration();
    private final ModManager modManager = new ModManager();
    private final PlayerDataManager playerDataManager = new PlayerDataManager();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private HttpServer server;

    public WebServerManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthWebManager");
        int port = plugin.getConfig().getInt("web.port", 8080);
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleRoot);
            server.createContext("/login", this::handleLogin);
            server.createContext("/logout", this::handleLogout);
            server.createContext("/info", this::handleInfo);
            server.createContext("/changepassword", this::handleChangePassword);
            server.createContext("/mods/download", this::handleModDownload);
            server.createContext("/mods", this::handleMods);
            server.createContext("/admin", this::handleAdmin);
            server.start();
        } catch (IOException e) {
            System.err.println("AuthWebManager: failed to start web server on port " + port + ": " + e.getMessage());
        }
    }

    public void save() {
        modManager.save();
    }

    public void shutdown() {
        if (server != null) {
            server.stop(0);
        }
        sessions.clear();
        modManager.save();
        modManager.shutdown();
    }

    private static class Session {
        private final String playerName;
        private long lastSeen;

        private Session(String playerName) {
            this.playerName = playerName;
            this.lastSeen = System.currentTimeMillis();
        }
    }

    private void handleRoot(HttpExchange exchange) throws IOException {
        redirect(exchange, "/info");
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> params = parseForm(exchange);
            String name = params.get("name");
            String password = params.get("password");
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                respond(exchange, 200, "text/html", page("登录", loginPage("请输入用户名和密码")));
                return;
            }
            Player player = Bukkit.getPlayerExact(name);
            if (player == null) {
                respond(exchange, 200, "text/html", page("登录", loginPage("玩家不在线，无法登录")));
                return;
            }
            if (!authMe.checkPassword(player, password)) {
                respond(exchange, 200, "text/html", page("登录", loginPage("密码错误")));
                return;
            }
            String sid = UUID.randomUUID().toString();
            sessions.put(sid, new Session(player.getName()));
            exchange.getResponseHeaders().add("Set-Cookie", "AUTHWM_SESSION=" + sid + "; Path=/; HttpOnly");
            redirect(exchange, "/info");
        } else {
            respond(exchange, 200, "text/html", page("登录", loginPage(null)));
        }
    }

    private void handleLogout(HttpExchange exchange) throws IOException {
        String sid = getSessionId(exchange);
        if (sid != null) {
            sessions.remove(sid);
        }
        exchange.getResponseHeaders().add("Set-Cookie", "AUTHWM_SESSION=; Path=/; Max-Age=0");
        redirect(exchange, "/login");
    }

    private void handleInfo(HttpExchange exchange) throws IOException {
        String playerName = requireLogin(exchange);
        if (playerName == null) {
            redirect(exchange, "/login");
            return;
        }
        Player player = Bukkit.getPlayerExact(playerName);
        boolean admin = player != null && (player.isOp() || player.hasPermission("authwebmanager.admin"));
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>个人信息</h2>");
        sb.append("<p>玩家名: ").append(escapeHtml(playerName)).append("</p>");
        sb.append("<p>在线状态: ").append(player != null ? "在线" : "离线").append("</p>");
        sb.append("<p><a href=\"/changepassword\">修改密码</a> | <a href=\"/mods\">模组列表</a>");
        if (admin) {
            sb.append(" | <a href=\"/admin\">管理员面板</a>");
        }
        sb.append(" | <a href=\"/logout\">退出登录</a></p>");
        respond(exchange, 200, "text/html", page("个人信息", sb.toString()));
    }

    private void handleChangePassword(HttpExchange exchange) throws IOException {
        String playerName = requireLogin(exchange);
        if (playerName == null) {
            redirect(exchange, "/login");
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, String> params = parseForm(exchange);
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            String msg;
            Player player = Bukkit.getPlayerExact(playerName);
            if (player == null) {
                msg = "玩家不在线，无法修改密码";
            } else if (oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                msg = "请填写完整";
            } else if (!authMe.checkPassword(player, oldPassword)) {
                msg = "原密码错误";
            } else if (newPassword.length() < 4) {
                msg = "新密码长度至少 4 位";
            } else {
                authMe.changePassword(player, newPassword);
                msg = "密码修改成功";
            }
            respond(exchange, 200, "text/html", page("修改密码", changePasswordPage(msg)));
        } else {
            respond(exchange, 200, "text/html", page("修改密码", changePasswordPage(null)));
        }
    }

    private void handleMods(HttpExchange exchange) throws IOException {
        if (requireLogin(exchange) == null) {
            redirect(exchange, "/login");
            return;
        }
        StringBuilder sb = new StringBuilder("<h2>模组列表</h2><table><tr><th>名称</th><th>版本</th><th>核心</th><th>下载</th></tr>");
        List<ModInfo> mods = modManager.listMods();
        if (mods != null) {
            for (ModInfo mod : mods) {
                sb.append("<tr><td>").append(escapeHtml(mod.getName())).append("</td>");
                sb.append("<td>").append(escapeHtml(mod.getVersion())).append("</td>");
                sb.append("<td>").append(escapeHtml(mod.getCore())).append("</td>");
                sb.append("<td><a href=\"/mods/download?name=").append(urlEncode(mod.getName())).append("\">下载</a></td></tr>");
            }
        }
        sb.append("</table><p><a href=\"/info\">返回</a></p>");
        respond(exchange, 200, "text/html", page("模组列表", sb.toString()));
    }

    private void handleModDownload(HttpExchange exchange) throws IOException {
        if (requireLogin(exchange) == null) {
            redirect(exchange, "/login");
            return;
        }
        String name = queryParam(exchange, "name");
        if (name == null || name.isEmpty()) {
            respond(exchange, 400, "text/plain", "缺少 name 参数");
            return;
        }
        File file = modManager.downloadMod(name);
        if (file == null || !file.isFile()) {
            respond(exchange, 404, "text/plain", "模组不存在");
            return;
        }
        byte[] data = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private void handleAdmin(HttpExchange exchange) throws IOException {
        String playerName = requireLogin(exchange);
        if (playerName == null) {
            redirect(exchange, "/login");
            return;
        }
        Player admin = Bukkit.getPlayerExact(playerName);
        if (admin == null || (!admin.isOp() && !admin.hasPermission("authwebmanager.admin"))) {
            respond(exchange, 200, "text/html", page("无权限", "<h2>无权限</h2><p>你不是管理员，无法访问该页面。</p><p><a href=\"/info\">返回</a></p>"));
            return;
        }
        if ("POST".equals(exchange.getRequestMethod())) {
            handleAdminPost(exchange);
            return;
        }
        respond(exchange, 200, "text/html", page("管理员面板", adminPage(null)));
    }

    private void handleAdminPost(HttpExchange exchange) throws IOException {
        Map<String, String> fields = new HashMap<>();
        Map<String, byte[]> files = new HashMap<>();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            int idx = contentType.indexOf("boundary=");
            String boundary = idx >= 0 ? contentType.substring(idx + 9) : "";
            if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                boundary = boundary.substring(1, boundary.length() - 1);
            }
            parseMultipart(exchange, boundary, fields, files);
        } else {
            fields.putAll(parseForm(exchange));
        }
        String action = fields.get("action");
        String msg;
        switch (action == null ? "" : action) {
            case "deletePlayer" -> {
                String name = fields.get("name");
                msg = (name != null && !name.isEmpty() && playerDataManager.deletePlayer(name))
                        ? "已删除玩家数据: " + name : "删除玩家数据失败";
            }
            case "resetPassword" -> {
                String name = fields.get("name");
                String newPassword = fields.get("newPassword");
                Player target = name == null ? null : Bukkit.getPlayerExact(name);
                if (target == null) {
                    msg = "玩家不在线，无法重置密码";
                } else if (newPassword == null || newPassword.isEmpty()) {
                    msg = "请填写新密码";
                } else if (newPassword.length() < 4) {
                    msg = "新密码长度至少 4 位";
                } else {
                    authMe.changePassword(target, newPassword);
                    msg = "已重置玩家密码: " + name;
                }
            }
            case "deleteMod" -> {
                String name = fields.get("name");
                msg = (name != null && !name.isEmpty() && modManager.deleteMod(name))
                        ? "已删除模组: " + name : "删除模组失败";
            }
            case "uploadMod" -> {
                String name = fields.get("name");
                String version = fields.get("version");
                String core = fields.get("core");
                byte[] data = files.get("file");
                if (name == null || name.isEmpty() || data == null || data.length == 0) {
                    msg = "上传失败，请填写名称并选择文件";
                } else {
                    msg = modManager.uploadMod(name, version, core, new ByteArrayInputStream(data))
                            ? "上传成功: " + name : "上传失败";
                }
            }
            default -> msg = "未知操作";
        }
        respond(exchange, 200, "text/html", page("管理员面板", adminPage(msg)));
    }

    private String loginPage(String error) {
        String msg = error == null ? "" : "<p><b>" + escapeHtml(error) + "</b></p>";
        return "<h2>登录</h2>" + msg
                + "<form method=\"post\" action=\"/login\">"
                + "<p>用户名: <input type=\"text\" name=\"name\" required></p>"
                + "<p>密码: <input type=\"password\" name=\"password\" required></p>"
                + "<p><button type=\"submit\">登录</button></p>"
                + "</form>";
    }

    private String changePasswordPage(String msg) {
        String m = msg == null ? "" : "<p><b>" + escapeHtml(msg) + "</b></p>";
        return "<h2>修改密码</h2>" + m
                + "<form method=\"post\" action=\"/changepassword\">"
                + "<p>原密码: <input type=\"password\" name=\"oldPassword\" required></p>"
                + "<p>新密码: <input type=\"password\" name=\"newPassword\" required></p>"
                + "<p><button type=\"submit\">确认修改</button></p>"
                + "</form><p><a href=\"/info\">返回</a></p>";
    }

    private String adminPage(String msg) {
        StringBuilder sb = new StringBuilder("<h2>管理员面板</h2>");
        if (msg != null) {
            sb.append("<p><b>").append(escapeHtml(msg)).append("</b></p>");
        }
        sb.append("<h3>玩家列表</h3><table><tr><th>玩家</th><th>在线</th><th>操作</th></tr>");
        List<OfflinePlayer> players = playerDataManager.getPlayers();
        if (players != null) {
            for (OfflinePlayer op : players) {
                String name = op.getName();
                if (name == null) {
                    continue;
                }
                sb.append("<tr><td>").append(escapeHtml(name)).append("</td><td>")
                        .append(op.isOnline() ? "在线" : "离线").append("</td><td>")
                        .append("<form method=\"post\" action=\"/admin\" style=\"display:inline\">")
                        .append("<input type=\"hidden\" name=\"action\" value=\"deletePlayer\">")
                        .append("<input type=\"hidden\" name=\"name\" value=\"").append(escapeHtml(name)).append("\">")
                        .append("<button type=\"submit\">删除数据</button></form> ")
                        .append("<form method=\"post\" action=\"/admin\" style=\"display:inline\">")
                        .append("<input type=\"hidden\" name=\"action\" value=\"resetPassword\">")
                        .append("<input type=\"hidden\" name=\"name\" value=\"").append(escapeHtml(name)).append("\">")
                        .append("<input type=\"password\" name=\"newPassword\" placeholder=\"新密码\" size=\"8\" required>")
                        .append("<button type=\"submit\">重置密码</button></form></td></tr>");
            }
        }
        sb.append("</table>");
        sb.append("<h3>模组列表</h3><table><tr><th>名称</th><th>版本</th><th>核心</th><th>操作</th></tr>");
        List<ModInfo> mods = modManager.listMods();
        if (mods != null) {
            for (ModInfo mod : mods) {
                sb.append("<tr><td>").append(escapeHtml(mod.getName())).append("</td>")
                        .append("<td>").append(escapeHtml(mod.getVersion())).append("</td>")
                        .append("<td>").append(escapeHtml(mod.getCore())).append("</td>")
                        .append("<td><form method=\"post\" action=\"/admin\" style=\"display:inline\">")
                        .append("<input type=\"hidden\" name=\"action\" value=\"deleteMod\">")
                        .append("<input type=\"hidden\" name=\"name\" value=\"").append(escapeHtml(mod.getName())).append("\">")
                        .append("<button type=\"submit\">删除</button></form></td></tr>");
            }
        }
        sb.append("</table>");
        sb.append("<h3>上传模组</h3>")
                .append("<form method=\"post\" action=\"/admin\" enctype=\"multipart/form-data\">")
                .append("<input type=\"hidden\" name=\"action\" value=\"uploadMod\">")
                .append("<p>名称: <input type=\"text\" name=\"name\" required></p>")
                .append("<p>版本: <input type=\"text\" name=\"version\"></p>")
                .append("<p>核心: <input type=\"text\" name=\"core\"></p>")
                .append("<p>文件: <input type=\"file\" name=\"file\" required></p>")
                .append("<p><button type=\"submit\">上传</button></p></form>")
                .append("<p><a href=\"/info\">返回</a></p>");
        return sb.toString();
    }

    private String requireLogin(HttpExchange exchange) {
        sweepSessions();
        String sid = getSessionId(exchange);
        if (sid == null) {
            return null;
        }
        Session session = sessions.get(sid);
        if (session == null) {
            return null;
        }
        if (System.currentTimeMillis() - session.lastSeen > SESSION_TIMEOUT) {
            sessions.remove(sid);
            return null;
        }
        session.lastSeen = System.currentTimeMillis();
        return session.playerName;
    }

    private void sweepSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(e -> now - e.getValue().lastSeen > SESSION_TIMEOUT);
    }

    private String getSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) {
            return null;
        }
        for (String cookie : cookies) {
            for (String part : cookie.split(";")) {
                part = part.trim();
                if (part.startsWith("AUTHWM_SESSION=")) {
                    return part.substring("AUTHWM_SESSION=".length());
                }
            }
        }
        return null;
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            return params;
        }
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                continue;
            }
            params.put(URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8),
                    URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
        }
        return params;
    }

    private void parseMultipart(HttpExchange exchange, String boundary, Map<String, String> fields, Map<String, byte[]> files) throws IOException {
        String text = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1);
        String[] parts = text.split("--" + boundary);
        for (String part : parts) {
            if (part.startsWith("--")) {
                continue;
            }
            int headerEnd = part.indexOf("\r\n\r\n");
            if (headerEnd < 0) {
                continue;
            }
            String headers = part.substring(0, headerEnd);
            String content = part.substring(headerEnd + 4);
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            }
            String fieldName = regexValue(headers, "name=\"([^\"]*)\"");
            String fileName = regexValue(headers, "filename=\"([^\"]*)\"");
            if (fieldName == null) {
                continue;
            }
            if (fileName != null && !fileName.isEmpty()) {
                files.put(fieldName, content.getBytes(StandardCharsets.ISO_8859_1));
            } else {
                fields.put(fieldName, new String(content.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
            }
        }
    }

    private String regexValue(String text, String regex) {
        Matcher m = Pattern.compile(regex).matcher(text);
        return m.find() ? m.group(1) : null;
    }

    private String queryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) {
                continue;
            }
            String k = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            if (key.equals(k)) {
                return URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private String page(String title, String content) {
        return "<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"UTF-8\"><title>" + escapeHtml(title)
                + "</title><style>"
                + "body{font-family:sans-serif;background:#f0f0f0;margin:30px}"
                + ".card{background:#fff;max-width:800px;margin:0 auto;padding:20px;border-radius:6px}"
                + "table{border-collapse:collapse;width:100%}td,th{border:1px solid #ccc;padding:6px;text-align:left}"
                + "input,button{padding:6px;margin:3px}"
                + "</style></head><body><div class=\"card\">" + content + "</div></body></html>";
    }

    private void respond(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }
}