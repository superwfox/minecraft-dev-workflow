package com.tahai.authweb.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class AuthMeDatabaseManager {
    private Connection connection;
    private final String type;
    private final String host;
    private final int port;
    private final String database;
    private final String dbUser;
    private final String dbPassword;
    private final String file;
    private final String table;

    public AuthMeDatabaseManager() {
        Plugin rawPlugin = Bukkit.getPluginManager().getPlugin("AuthWeb");
        if (rawPlugin instanceof JavaPlugin plugin) {
            type = plugin.getConfig().getString("database.type", "sqlite");
            host = plugin.getConfig().getString("database.host", "localhost");
            port = plugin.getConfig().getInt("database.port", 3306);
            database = plugin.getConfig().getString("database.name", "authme");
            dbUser = plugin.getConfig().getString("database.username", "root");
            dbPassword = plugin.getConfig().getString("database.password", "");
            file = plugin.getConfig().getString("database.file", new File(plugin.getDataFolder(), "authme.db").getPath());
            table = plugin.getConfig().getString("database.table", "authme");
        } else {
            type = "sqlite";
            host = "localhost";
            port = 3306;
            database = "authme";
            dbUser = "root";
            dbPassword = "";
            file = "authme.db";
            table = "authme";
        }
        connect();
    }

    private void connect() {
        try {
            if ("mysql".equalsIgnoreCase(type)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8";
                connection = DriverManager.getConnection(url, dbUser, dbPassword);
            } else {
                Class.forName("org.sqlite.JDBC");
                File dbFile = new File(file);
                if (dbFile.getParentFile() != null) {
                    dbFile.getParentFile().mkdirs();
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getPath());
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "AuthWeb: 数据库连接失败", e);
        }
    }

    public String getPasswordHash(String username) {
        if (connection == null) return null;
        String sql = "SELECT password FROM " + table + " WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase(Locale.ROOT));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "AuthWeb: 查询密码哈希失败", e);
        }
        return null;
    }

    public boolean verifyPassword(String username, String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) return false;
        String hash = getPasswordHash(username);
        if (hash == null || hash.isEmpty()) return false;
        return checkPassword(hash, rawPassword);
    }

    public boolean setPassword(String username, String rawPassword) {
        if (connection == null || rawPassword == null || rawPassword.isEmpty()) return false;
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        String sql = "UPDATE " + table + " SET password = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, hash);
            stmt.setString(2, username.toLowerCase(Locale.ROOT));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "AuthWeb: 修改密码失败", e);
        }
        return false;
    }

    public boolean deleteAccount(String username) {
        if (connection == null) return false;
        String sql = "DELETE FROM " + table + " WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username.toLowerCase(Locale.ROOT));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "AuthWeb: 删除账户失败", e);
        }
        return false;
    }

    public List<String> getPlayerList() {
        List<String> players = new ArrayList<>();
        if (connection == null) return players;
        String sql = "SELECT username FROM " + table;
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                players.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "AuthWeb: 获取玩家列表失败", e);
        }
        return players;
    }

    public void save() {
    }

    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private boolean checkPassword(String hash, String raw) {
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            return BCrypt.checkpw(raw, hash);
        }
        if (hash.startsWith("$SHA1$")) {
            return checkSha(hash, raw, "SHA-1");
        }
        if (hash.startsWith("$SHA$") || hash.startsWith("$SHA256$")) {
            return checkSha(hash, raw, "SHA-256");
        }
        if (hash.startsWith("$SHA512$")) {
            return checkSha(hash, raw, "SHA-512");
        }
        return false;
    }

    private boolean checkSha(String hash, String raw, String algorithm) {
        String[] parts = hash.split("\\$");
        if (parts.length < 4) return false;
        String salt = parts[parts.length - 2];
        String expected = parts[parts.length - 1];
        String candidate1 = hashHex(algorithm, raw + salt);
        String candidate2 = hashHex(algorithm, hashHex(algorithm, raw) + salt);
        return MessageDigest.isEqual(candidate1.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))
                || MessageDigest.isEqual(candidate2.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    private String hashHex(String algorithm, String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xFF));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
}