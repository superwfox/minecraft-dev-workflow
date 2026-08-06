package com.tahai.joinultra;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.Map;

public class JoinListener implements Listener {
    private final Plugin plugin;
    private final ConfigManager configManager;

    public JoinListener() {
        this.plugin = Bukkit.getPluginManager().getPlugin("JoinUltra");
        this.configManager = new ConfigManager(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (configManager.isDisableVanillaMessage()) {
            event.setJoinMessage(null);
        }

        Map<String, Map<String, String>> groups = configManager.getGroups();
        if (groups == null) return;

        Map<String, String> settings = null;
        for (Map.Entry<String, Map<String, String>> entry : groups.entrySet()) {
            Map<String, String> group = entry.getValue();
            if (hasPermissions(player, group.get("permissions"))) {
                settings = group;
                break;
            }
        }
        if (settings == null) {
            settings = groups.get(configManager.getDefaultGroup());
        }
        if (settings == null) return;

        String chatMessage = settings.get("chat-message");
        if (chatMessage != null && !chatMessage.isEmpty()) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(parsePlaceholders(player, chatMessage)));
        }

        String title = settings.get("title");
        String subtitle = settings.get("subtitle");
        if (title != null || subtitle != null) {
            Component titleComponent = title == null ? Component.empty() : MiniMessage.miniMessage().deserialize(parsePlaceholders(player, title));
            Component subtitleComponent = subtitle == null ? Component.empty() : MiniMessage.miniMessage().deserialize(parsePlaceholders(player, subtitle));
            player.showTitle(Title.title(titleComponent, subtitleComponent, Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))));
        }

        String bossbarText = settings.get("bossbar-text");
        if (bossbarText != null && !bossbarText.isEmpty()) {
            Component bossbarComponent = MiniMessage.miniMessage().deserialize(parsePlaceholders(player, bossbarText));
            BarColor color;
            try {
                color = BarColor.valueOf(settings.getOrDefault("bossbar-color", "WHITE").toUpperCase());
            } catch (Exception e) {
                color = BarColor.WHITE;
            }
            BarStyle style;
            try {
                style = BarStyle.valueOf(settings.getOrDefault("bossbar-style", "SOLID").toUpperCase());
            } catch (Exception e) {
                style = BarStyle.SOLID;
            }
            BossBar bossBar = Bukkit.createBossBar(LegacyComponentSerializer.legacySection().serialize(bossbarComponent), color, style, 1.0f);
            bossBar.addPlayer(player);
            int duration;
            try {
                duration = Integer.parseInt(settings.getOrDefault("bossbar-duration", settings.getOrDefault("duration", "100")));
            } catch (NumberFormatException e) {
                duration = 100;
            }
            new BossBarRemoveTask(bossBar, player).runTaskLater(plugin, Math.max(0, duration));
        }
    }

    private boolean hasPermissions(Player player, String permissions) {
        if (permissions == null || permissions.trim().isEmpty()) return false;
        for (String line : permissions.split("\\R")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            boolean all = true;
            for (String permission : line.split("\\s+")) {
                if (!player.hasPermission(permission)) {
                    all = false;
                    break;
                }
            }
            if (all) return true;
        }
        return false;
    }

    private String parsePlaceholders(Player player, String text) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }
}