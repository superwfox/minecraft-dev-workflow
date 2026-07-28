package com.tahai.wqltab;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

public class JoinListener implements Listener {

    private static ConfigManager configManager;

    /**
     * 插件主类在注册此监听器之前必须调用此方法注入 ConfigManager 实例。
     */
    public static void setConfigManager(ConfigManager cm) {
        configManager = cm;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (configManager == null) return;

        // 获取 header/footer
        List<String> headerList = configManager.getHeader();
        List<String> footerList = configManager.getFooter();
        String header = parsePlaceholders(player, String.join("\n", headerList));
        String footer = parsePlaceholders(player, String.join("\n", footerList));
        header = ChatColor.translateAlternateColorCodes('§', header);
        footer = ChatColor.translateAlternateColorCodes('§', footer);
        player.setPlayerListHeaderFooter(header, footer);

        // 计分板
        String titleRaw = configManager.getScoreboardTitle();
        String title = parsePlaceholders(player, titleRaw);
        title = ChatColor.translateAlternateColorCodes('§', title);
        List<String> lines = configManager.getScoreboardLines();

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        Scoreboard board = manager.getNewScoreboard();
        Objective obj = board.registerNewObjective("wqltab", Criteria.DUMMY, title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int score = lines.size();
        for (String line : lines) {
            String parsed = parsePlaceholders(player, line);
            parsed = ChatColor.translateAlternateColorCodes('§', parsed);
            obj.getScore(parsed).setScore(score--);
        }

        player.setScoreboard(board);
    }

    private String parsePlaceholders(Player player, String text) {
        if (text == null) return "";
        // 先解析 Vault 经济变量
        text = applyVaultPlaceholders(player, text);
        // 再解析 PlaceholderAPI 变量
        text = applyPlaceholderAPI(player, text);
        return text;
    }

    /**
     * 替换 Vault 经济占位符，目前支持：
     *   %vault_balance%              原始余额 (double -> 字符串)
     *   %vault_balance_formatted%    格式化的货币字符串
     */
    private String applyVaultPlaceholders(Player player, String text) {
        // 尝试获取 Economy 服务
        Economy economy = null;
        try {
            if (Bukkit.getServer().getPluginManager().isPluginEnabled("Vault")) {
                RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                }
            }
        } catch (NoClassDefFoundError ignored) {
            // Vault 未加载，跳过
        }

        if (economy != null) {
            if (text.contains("%vault_balance%")) {
                double balance = economy.getBalance(player);
                text = text.replace("%vault_balance%", String.valueOf(balance));
            }
            if (text.contains("%vault_balance_formatted%")) {
                double balance = economy.getBalance(player);
                String formatted = economy.format(balance);
                text = text.replace("%vault_balance_formatted%", formatted != null ? formatted : String.valueOf(balance));
            }
        }

        return text;
    }

    /**
     * 应用 PlaceholderAPI 占位符
     */
    private String applyPlaceholderAPI(Player player, String text) {
        org.bukkit.plugin.Plugin papiPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papiPlugin != null && papiPlugin.isEnabled()) {
            try {
                Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Method method = clazz.getMethod("setPlaceholders", Player.class, String.class);
                return (String) method.invoke(null, player, text);
            } catch (Exception ignored) {
                // 回退到原始字符串
            }
        }
        return text;
    }
}