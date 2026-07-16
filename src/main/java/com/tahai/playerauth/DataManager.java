package com.tahai.playerauth;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class DataManager {

    private final Plugin plugin;
    private final Map<String, String> passwords = new HashMap<>();
    private final Set<UUID> loggedInPlayers = new HashSet<>();

    private String registerSuccess;
    private String loginSuccess;
    private String wrongPassword;
    private String notLoggedIn;
    private String commandRestricted;
    private String passwordTooShort;
    private String passwordsDoNotMatch;

    private static final int MIN_PASSWORD_LENGTH = 4;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        loadMessages();
        loadPasswords();
    }

    private void loadMessages() {
        FileConfiguration config = plugin.getConfig();
        registerSuccess = config.getString("messages.register-success", ChatColor.YELLOW + "注册成功！");
        loginSuccess = config.getString("messages.login-success", ChatColor.YELLOW + "登录成功！");
        wrongPassword = config.getString("messages.wrong-password", ChatColor.AQUA + "密码错误！");
        notLoggedIn = config.getString("messages.not-logged-in", ChatColor.AQUA + "您尚未登录！");
        commandRestricted = config.getString("messages.command-restricted", ChatColor.AQUA + "请先登录！");
        passwordTooShort = config.getString("messages.password-too-short", ChatColor.AQUA + "密码太短！");
        passwordsDoNotMatch = config.getString("messages.passwords-do-not-match", ChatColor.AQUA + "两次密码不一致！");
    }

    private void loadPasswords() {
        File dataFile = new File(plugin.getDataFolder(), "players.csv");
        if (!dataFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    passwords.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法加载玩家数据", e);
        }
    }

    public void save() {
        File dataFile = new File(plugin.getDataFolder(), "players.csv");
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
                for (Map.Entry<String, String> entry : passwords.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "无法保存玩家数据", e);
        }
    }

    public void shutdown() {
        save();
    }

    public String getRegisterSuccess() { return registerSuccess; }
    public String getLoginSuccess() { return loginSuccess; }
    public String getWrongPassword() { return wrongPassword; }
    public String getNotLoggedIn() { return notLoggedIn; }
    public String getCommandRestricted() { return commandRestricted; }
    public String getPasswordTooShort() { return passwordTooShort; }
    public String getPasswordsDoNotMatch() { return passwordsDoNotMatch; }

    public String register(String username, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return passwordsDoNotMatch;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return passwordTooShort;
        }
        if (passwords.containsKey(username)) {
            return ChatColor.AQUA + "该用户名已被注册！";
        }
        passwords.put(username, password);
        return null;
    }

    public boolean login(UUID uuid, String username, String password) {
        String stored = passwords.get(username);
        if (stored == null || !stored.equals(password)) {
            return false;
        }
        loggedInPlayers.add(uuid);
        return true;
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedInPlayers.contains(uuid);
    }

    public void addLoggedIn(UUID uuid) {
        loggedInPlayers.add(uuid);
    }

    public void removeLoggedIn(UUID uuid) {
        loggedInPlayers.remove(uuid);
    }
}