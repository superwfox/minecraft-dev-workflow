package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class WarKillListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (victim.getKiller() == null) return;

        Player killer = victim.getKiller();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return;

        FileConfiguration config = plugin.getConfig();
        String attacker = config.getString("war.attacker", "");
        String defender = config.getString("war.defender", "");
        if (attacker.isEmpty() || defender.isEmpty()) return;

        String victimGuild = findGuild(config, victim);
        String killerGuild = findGuild(config, killer);
        if (victimGuild == null || killerGuild == null) return;
        if (victimGuild.equals(killerGuild)) return;

        boolean victimAttacker = victimGuild.equals(attacker);
        boolean victimDefender = victimGuild.equals(defender);
        boolean killerAttacker = killerGuild.equals(attacker);
        boolean killerDefender = killerGuild.equals(defender);
        if ((victimAttacker && killerDefender) || (victimDefender && killerAttacker)) {
            int kills = config.getInt("guilds." + killerGuild + ".kills", 0) + 1;
            config.set("guilds." + killerGuild + ".kills", kills);
            plugin.saveConfig();
        }
    }

    private String findGuild(FileConfiguration config, Player player) {
        ConfigurationSection guilds = config.getConfigurationSection("guilds");
        if (guilds == null) return null;

        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        for (String guild : guilds.getKeys(false)) {
            String leader = config.getString("guilds." + guild + ".leader");
            if (leader != null && (leader.equals(uuid) || leader.equalsIgnoreCase(name))) {
                return guild;
            }

            ConfigurationSection members = config.getConfigurationSection("guilds." + guild + ".members");
            if (members != null) {
                for (String key : members.getKeys(false)) {
                    if (key.equals(uuid) || key.equalsIgnoreCase(name)) {
                        return guild;
                    }
                }
            }
        }
        return null;
    }
}