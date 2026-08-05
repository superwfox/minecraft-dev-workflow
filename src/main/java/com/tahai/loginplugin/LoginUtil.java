package com.tahai.loginplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class LoginUtil {

    private static NamespacedKey passwordKey;
    private static NamespacedKey loggedInKey;
    private static NamespacedKey errorCountKey;
    private static NamespacedKey tempPasswordKey;

    private LoginUtil() {}

    public static void init(Plugin plugin) {
        if (passwordKey != null) return;
        passwordKey = new NamespacedKey(plugin, "password");
        loggedInKey = new NamespacedKey(plugin, "logged_in");
        errorCountKey = new NamespacedKey(plugin, "error_count");
        tempPasswordKey = new NamespacedKey(plugin, "temp_password");
    }

    private static void checkInitialized() {
        if (passwordKey == null) {
            throw new IllegalStateException("LoginUtil has not been initialized");
        }
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getPassword(Player player) {
        checkInitialized();
        return player.getPersistentDataContainer().get(passwordKey, PersistentDataType.STRING);
    }

    public static void setPassword(Player player, String rawPassword) {
        checkInitialized();
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (rawPassword == null) {
            container.remove(passwordKey);
        } else {
            container.set(passwordKey, PersistentDataType.STRING, sha256(rawPassword));
        }
    }

    public static boolean isLoggedIn(Player player) {
        checkInitialized();
        return Boolean.TRUE.equals(player.getPersistentDataContainer().get(loggedInKey, PersistentDataType.BOOLEAN));
    }

    public static void setLoggedIn(Player player, boolean loggedIn) {
        checkInitialized();
        player.getPersistentDataContainer().set(loggedInKey, PersistentDataType.BOOLEAN, loggedIn);
    }

    public static int getErrorCount(Player player) {
        checkInitialized();
        Integer count = player.getPersistentDataContainer().get(errorCountKey, PersistentDataType.INTEGER);
        return count == null ? 0 : count;
    }

    public static void setErrorCount(Player player, int count) {
        checkInitialized();
        player.getPersistentDataContainer().set(errorCountKey, PersistentDataType.INTEGER, count);
    }

    public static String getTempPassword(Player player) {
        checkInitialized();
        return player.getPersistentDataContainer().get(tempPasswordKey, PersistentDataType.STRING);
    }

    public static void setTempPassword(Player player, String rawPassword) {
        checkInitialized();
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (rawPassword == null) {
            container.remove(tempPasswordKey);
        } else {
            container.set(tempPasswordKey, PersistentDataType.STRING, sha256(rawPassword));
        }
    }
}