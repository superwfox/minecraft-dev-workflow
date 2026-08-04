package com.tahai.authweb.manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.tahai.authweb.model.ModInfo;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServerManager {

    private final Plugin plugin;
    private final AuthMeDatabaseManager authMeDB;
    private final ModManager modManager;
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private HttpServer server;
    private ExecutorService executor;

    public HttpServerManager(AuthMeDatabaseManager authMeDB, ModManager modManager) {
        this.plugin = Bukkit.getPluginManager().getPlugin("AuthWeb");
        this.authMeDB = authMeDB;
        this.modManager = modManager;
        if (plugin == null) {
            throw new IllegalStateException("AuthWeb plugin not found");
        }

        String host = plugin.getConfig().getString("http.host", "0.0.0.0");
        int port = plugin.getConfig().getInt("http.port", 8080);

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/", this::handle);
            executor = Executors.newCachedThreadPool();
            server.setExecutor(executor);
            server.start();
            plugin.getLogger().info("AuthWeb HTTP server listening on " + host + ":" + port);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start AuthWeb HTTP server: " + e.getMessage());
        }
    }

    public void save() {
    }

    public void shutdown() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
        sessions.clear();
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if (path.equals("/")) {
                redirect(exchange, "/login");
                return;
            }
            if (path.equals("/login")) {
                if (method.equals("POST")) {
                    handleLogin(exchange);
                } else {
                    sendHtml(exchange, 200, loginPage(null));
                }
                return;
            }
            if (path.equals("/profile") && method.equals("GET")) {
                handleProfile(exchange);
                return;
            }
            if (path.equals("/change-pwd") && method.equals("POST")) {
                handleChangePwd(exchange);
                return;
            }
            if (path.equals("/mods") && method.equals("GET")) {
                handleMods(exchange);
                return;
            }
            if (path.startsWith("/mods/download/")) {
                handleDownload(exchange, path.substring("/mods/download/".length()));
                return;
            }
            if (path.equals("/admin/players") && method.equals("GET")) {
                handleAdminPlayers(exchange);
                return;
            }
            if (path.equals("/admin/delete-player") && method.equals("POST")) {
                handleAdminDeletePlayer(exchange);
                return;
            }
            if (path.equals("/admin/reset-pwd") && method.equals("POST")) {
                handleAdminResetPwd(exchange);
                return;
            }
            if (path.equals("/admin/upload-mod") && method.equals("POST")) {
                handleAdminUploadMod(exchange);
                return;
            }
            if (path.equals("/admin/delete-mod") && method.equals("POST")) {
                handleAdminDeleteMod(exchange);
                return;
            }

            sendHtml(exchange, 404, page("Not Found", "404 Not Found"));
        } catch (Exception e) {
            plugin.getLogger().warning("HTTP handler error: " + e.getMessage());
            try {
                sendHtml(exchange, 500, page("Error", "Internal Server Error"));
            } catch (IOException ignored) {
            }
        } finally {
            exchange.close();
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "");
        String password = form.getOrDefault("password", "");

        if (username.isEmpty() || password.isEmpty()) {
            sendHtml(exchange, 400, loginPage("Username and password are required."));
            return;
        }

        if (authMeDB.verifyPassword(username, password)) {
            setSessionCookie(exchange, username);
            redirect(exchange, "/profile");
        } else {
            sendHtml(exchange, 401, loginPage("Invalid username or password."));
        }
    }

    private void handleProfile(HttpExchange exchange) throws IOException {
        String user = requireLogin(exchange);
        if (user == null) return;

        String content = "<h2>Welcome, " + esc(user) + "</h2>"
                + "<p><a href=\"/mods\">Mods</a></p>"
                + "<p><a href=\"/admin/players\">Admin Panel</a></p>"
                + "<h3>Change Password</h3>"
                + "<form method=\"post\" action=\"/change-pwd\">"
                + "<p>Old password: <input type=\"password\" name=\"oldPassword\"></p>"
                + "<p>New password: <input type=\"password\" name=\"newPassword\"></p>"
                + "<button type=\"submit\">Change Password</button></form>";
        sendHtml(exchange, 200, page("Profile", content));
    }

    private void handleChangePwd(HttpExchange exchange) throws IOException {
        String user = requireLogin(exchange);
        if (user == null) return;

        Map<String, String> form = parseForm(exchange);
        String oldPassword = form.getOrDefault("oldPassword", "");
        String newPassword = form.getOrDefault("newPassword", "");

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            sendHtml(exchange, 400, page("Error", "Old and new passwords are required."));
            return;
        }

        if (!authMeDB.verifyPassword(user, oldPassword)) {
            sendHtml(exchange, 400, page("Error", "Old password is incorrect."));
            return;
        }

        if (authMeDB.setPassword(user, newPassword)) {
            sendHtml(exchange, 200, page("Success", "Password changed successfully. <a href=\"/profile\">Back</a>"));
        } else {
            sendHtml(exchange, 500, page("Error", "Failed to update password."));
        }
    }

    private void handleMods(HttpExchange exchange) throws IOException {
        if (requireLogin(exchange) == null) return;

        Collection<ModInfo> mods = modManager.getAllMods();
        StringBuilder table = new StringBuilder()
                .append("<table border=\"1\" cellpadding=\"4\">")
                .append("<tr><th>ID</th><th>Name</th><th>Version</th><th>MC Version</th><th>Download</th></tr>");

        if (mods.isEmpty()) {
            table.append("<tr><td colspan=\"5\">No mods registered.</td></tr>");
        }

        for (ModInfo mod : mods) {
            String id = mod.getId() == null ? "" : mod.getId();
            String name = mod.getName() == null ? "" : mod.getName();
            String version = mod.getModVersion() == null ? "" : mod.getModVersion();
            String mcVersion = mod.getMinecraftVersion() == null ? "" : mod.getMinecraftVersion();
            String encodedId = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");

            table.append("<tr>")
                    .append("<td>").append(esc(id)).append("</td>")
                    .append("<td>").append(esc(name)).append("</td>")
                    .append("<td>").append(esc(version)).append("</td>")
                    .append("<td>").append(esc(mcVersion)).append("</td>")
                    .append("<td><a href=\"/mods/download/").append(encodedId).append("\">Download</a></td>")
                    .append("</tr>");
        }

        table.append("</table>");
        sendHtml(exchange, 200, page("Mods", "<h2>Mods</h2><p><a href=\"/profile\">Back</a></p>" + table));
    }

    private void handleDownload(HttpExchange exchange, String encodedId) throws IOException {
        if (requireLogin(exchange) == null) return;

        String id = URLDecoder.decode(encodedId, StandardCharsets.UTF_8);
        String filePath = modManager.getModFilePath(id);

        if (filePath == null) {
            sendHtml(exchange, 404, page("Not Found", "Mod not found."));
            return;
        }

        File file = new File(filePath);
        if (!file.isFile()) {
            sendHtml(exchange, 404, page("Not Found", "File not found."));
            return;
        }

        String encodedName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        exchange.sendResponseHeaders(200, file.length());

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(file.toPath(), os);
        }
    }

    private void handleAdminPlayers(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) return;

        List<String[]> players = getPlayerFiles();
        StringBuilder rows = new StringBuilder();

        if (players.isEmpty()) {
            rows.append("<tr><td colspan=\"2\">No player data found.</td></tr>");
        } else {
            for (String[] player : players) {
                rows.append("<tr><td>").append(esc(player[0])).append("</td><td>").append(esc(player[1])).append("</td></tr>");
            }
        }

        String content = "<h2>Admin Panel</h2>"
                + "<p><a href=\"/profile\">Back to profile</a></p>"
                + "<h3>Players</h3>"
                + "<table border=\"1\" cellpadding=\"4\"><tr><th>UUID</th><th>Name</th></tr>" + rows + "</table>"
                + adminForms();

        sendHtml(exchange, 200, page("Admin", content));
    }

    private void handleAdminDeletePlayer(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) return;

        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();

        if (username.isEmpty()) {
            sendHtml(exchange, 400, page("Error", "Username is required."));
            return;
        }

        boolean cleared = clearPlayerData(username);
        boolean deleted = authMeDB.deleteAccount(username);

        String result = "Username: " + esc(username) + "<br>Inventory cleared: " + cleared
                + "<br>AuthMe account deleted: " + deleted;
        sendHtml(exchange, 200, page("Result", "<p>" + result + "</p><p><a href=\"/admin/players\">Back</a></p>"));
    }

    private void handleAdminResetPwd(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) return;

        Map<String, String> form = parseForm(exchange);
        String username = form.getOrDefault("username", "").trim();
        String newPassword = form.getOrDefault("newPassword", "");

        if (username.isEmpty() || newPassword.isEmpty()) {
            sendHtml(exchange, 400, page("Error", "Username and new password are required."));
            return;
        }

        if (authMeDB.setPassword(username, newPassword)) {
            sendHtml(exchange, 200, page("Success", "Password reset for " + esc(username) + ". <a href=\"/admin/players\">Back</a>"));
        } else {
            sendHtml(exchange, 500, page("Error", "Failed to reset password."));
        }
    }

    private void handleAdminUploadMod(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) return;

        Map<String, Object> fields = parseMultipart(exchange);
        String id = str(fields.get("id")).trim();
        String name = str(fields.get("name")).trim();
        String modVersion = str(fields.get("modVersion")).trim();
        String minecraftVersion = str(fields.get("minecraftVersion")).trim();
        String fileName = str(fields.get("filename")).trim();
        byte[] fileBytes = (byte[]) fields.get("file");

        if (id.isEmpty() || name.isEmpty() || modVersion.isEmpty() || minecraftVersion.isEmpty()
                || fileBytes == null || fileBytes.length == 0) {
            sendHtml(exchange, 400, page("Error", "All fields and a file are required."));
            return;
        }

        if (fileName.isEmpty()) {
            fileName = id + ".jar";
        }

        plugin.getDataFolder().mkdirs();
        File temp = File.createTempFile("mod-upload-", ".tmp", plugin.getDataFolder());

        try {
            Files.write(temp.toPath(), fileBytes);
            ModInfo mod = modManager.uploadMod(temp, id, name, modVersion, minecraftVersion, fileName, plugin);
            if (mod == null) {
                sendHtml(exchange, 500, page("Error", "Upload failed."));
                return;
            }
            modManager.save();
            sendHtml(exchange, 200, page("Success", "Mod uploaded: " + esc(mod.getId()) + " <a href=\"/admin/players\">Back</a>"));
        } finally {
            temp.delete();
        }
    }

    private void handleAdminDeleteMod(HttpExchange exchange) throws IOException {
        if (requireAdmin(exchange) == null) return;

        Map<String, String> form = parseForm(exchange);
        String id = form.getOrDefault("id", "").trim();

        if (id.isEmpty()) {
            sendHtml(exchange, 400, page("Error", "Mod ID is required."));
            return;
        }

        boolean deleted = modManager.deleteMod(id);
        modManager.save();
        String msg = deleted ? "Mod deleted: " + esc(id) : "Mod not found: " + esc(id);
        sendHtml(exchange, 200, page("Result", "<p>" + msg + "</p><p><a href=\"/admin/players\">Back</a></p>"));
    }

    private String requireLogin(HttpExchange exchange) throws IOException {
        String user = getSessionUser(exchange);
        if (user == null) {
            redirect(exchange, "/login");
        }
        return user;
    }

    private String requireAdmin(HttpExchange exchange) throws IOException {
        String user = requireLogin(exchange);
        if (user == null) return null;

        if (!isAdmin(user)) {
            sendHtml(exchange, 403, page("Forbidden", "You do not have admin access."));
            return null;
        }
        return user;
    }

    private boolean isAdmin(String username) {
        List<String> admins = plugin.getConfig().getStringList("http.adminUsers");
        if (admins.contains(username)) return true;

        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(username);
                return op.isOp();
            }).get();
        } catch (Exception e) {
            return false;
        }
    }

    private List<String[]> getPlayerFiles() {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                List<String[]> result = new ArrayList<>();
                if (Bukkit.getWorlds().isEmpty()) return result;

                File dir = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "playerdata");
                File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
                if (files == null) return result;

                for (File file : files) {
                    String uuidStr = file.getName().substring(0, file.getName().length() - 4);
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                        String name = op.getName();
                        result.add(new String[]{uuidStr, name == null ? "Unknown" : name});
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                return result;
            }).get();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private boolean clearPlayerData(String username) {
        try {
            return Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                Player player = Bukkit.getPlayerExact(username);

                if (player != null) {
                    player.getInventory().clear();
                    player.getEnderChest().clear();
                    player.saveData();
                    return true;
                }

                OfflinePlayer op = Bukkit.getOfflinePlayer(username);
                UUID uuid = op.getUniqueId();
                if (uuid == null) return false;

                boolean cleared = false;
                for (World world : Bukkit.getWorlds()) {
                    File f = new File(new File(world.getWorldFolder(), "playerdata"), uuid + ".dat");
                    if (f.exists()) {
                        cleared |= f.delete();
                    }
                }
                return cleared;
            }).get();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, String> parseForm(HttpExchange exchange) throws IOException {
        Map<String, String> params = new HashMap<>();
        String query = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        for (String pair : query.split("&")) {
            if (pair.isEmpty()) continue;
            int idx = pair.indexOf('=');
            if (idx < 0) {
                params.put(URLDecoder.decode(pair, StandardCharsets.UTF_8), "");
            } else {
                params.put(
                        URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8),
                        URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    private Map<String, Object> parseMultipart(HttpExchange exchange) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
            return result;
        }

        int boundaryIndex = contentType.toLowerCase().indexOf("boundary=");
        if (boundaryIndex < 0) return result;

        String boundary = contentType.substring(boundaryIndex + 9).trim();
        if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
            boundary = boundary.substring(1, boundary.length() - 1);
        }

        byte[] boundaryBytes = ("--" + boundary).getBytes(StandardCharsets.UTF_8);
        byte[] body = exchange.getRequestBody().readAllBytes();
        byte[] headerEndBytes = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        int pos = indexOf(body, boundaryBytes, 0);
        while (pos != -1) {
            int start = pos + boundaryBytes.length;

            if (start + 1 < body.length && body[start] == '-' && body[start + 1] == '-') {
                break;
            }
            if (start < body.length && body[start] == '\r' && start + 1 < body.length && body[start + 1] == '\n') {
                start += 2;
            } else if (start < body.length && body[start] == '\n') {
                start += 1;
            }

            int headerEnd = indexOf(body, headerEndBytes, start);
            if (headerEnd == -1) break;

            String headerText = new String(body, start, headerEnd - start, StandardCharsets.UTF_8);
            int contentStart = headerEnd + 4;
            int nextBoundary = indexOf(body, boundaryBytes, contentStart);
            if (nextBoundary == -1) break;

            int contentEnd = nextBoundary;
            if (contentEnd >= 2 && body[contentEnd - 2] == '\r' && body[contentEnd - 1] == '\n') {
                contentEnd -= 2;
            } else if (contentEnd >= 1 && body[contentEnd - 1] == '\n') {
                contentEnd -= 1;
            }

            byte[] partBody = Arrays.copyOfRange(body, contentStart, contentEnd);
            String name = null;
            String filename = null;

            for (String line : headerText.split("\r\n")) {
                if (line.toLowerCase().startsWith("content-disposition:")) {
                    String[] attrs = line.substring(line.indexOf(':') + 1).split(";");
                    for (String attr : attrs) {
                        attr = attr.trim();
                        if (attr.startsWith("name=")) {
                            name = unquote(attr.substring(5));
                        } else if (attr.startsWith("filename=")) {
                            filename = unquote(attr.substring(9));
                        }
                    }
                }
            }

            if (name != null) {
                if (filename != null) {
                    result.put(name, partBody);
                    result.put("filename", filename);
                } else {
                    result.put(name, new String(partBody, StandardCharsets.UTF_8));
                }
            }

            pos = nextBoundary;
        }

        return result;
    }

    private int indexOf(byte[] data, byte[] pattern, int from) {
        outer:
        for (int i = from; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private String getSessionUser(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;

        for (String cookie : cookies) {
            for (String part : cookie.split(";")) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals("session")) {
                    return sessions.get(kv[1]);
                }
            }
        }
        return null;
    }

    private void setSessionCookie(HttpExchange exchange, String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, username);
        exchange.getResponseHeaders().add("Set-Cookie", "session=" + token + "; Path=/; HttpOnly");
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
    }

    private void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String adminForms() {
        return "<h3>Delete Player</h3>"
                + "<form method=\"post\" action=\"/admin/delete-player\">"
                + "<input name=\"username\" placeholder=\"Username\">"
                + "<button type=\"submit\">Delete Player</button></form>"

                + "<h3>Reset Password</h3>"
                + "<form method=\"post\" action=\"/admin/reset-pwd\">"
                + "<input name=\"username\" placeholder=\"Username\"><br>"
                + "<input type=\"password\" name=\"newPassword\" placeholder=\"New password\">"
                + "<button type=\"submit\">Reset Password</button></form>"

                + "<h3>Upload Mod</h3>"
                + "<form method=\"post\" action=\"/admin/upload-mod\" enctype=\"multipart/form-data\">"
                + "<input name=\"id\" placeholder=\"Mod ID\"><br>"
                + "<input name=\"name\" placeholder=\"Mod name\"><br>"
                + "<input name=\"modVersion\" placeholder=\"Mod version\"><br>"
                + "<input name=\"minecraftVersion\" placeholder=\"Minecraft version\"><br>"
                + "<input type=\"file\" name=\"file\"><br>"
                + "<button type=\"submit\">Upload</button></form>"

                + "<h3>Delete Mod</h3>"
                + "<form method=\"post\" action=\"/admin/delete-mod\">"
                + "<input name=\"id\" placeholder=\"Mod ID\">"
                + "<button type=\"submit\">Delete Mod</button></form>";
    }

    private String loginPage(String error) {
        String errorHtml = error == null ? "" : "<p style=\"color:#b00\">" + esc(error) + "</p>";
        return page("Login",
                "<h2>AuthWeb Login</h2>"
                        + errorHtml
                        + "<form method=\"post\" action=\"/login\">"
                        + "<p>Username: <input name=\"username\"></p>"
                        + "<p>Password: <input type=\"password\" name=\"password\"></p>"
                        + "<button type=\"submit\">Login</button></form>");
    }

    private String page(String title, String content) {
        return "<!DOCTYPE html><html lang=\"zh\"><head><meta charset=\"UTF-8\"><title>"
                + esc(title)
                + "</title></head><body>"
                + content
                + "</body></html>";
    }

    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }
}