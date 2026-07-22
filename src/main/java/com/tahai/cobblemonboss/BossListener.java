package com.tahai.cobblemonboss;

import com.cobblemon.mod.common.api.pokemon.Pokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.PokemonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.util.*;

public class BossListener implements Listener {

    private static final NamespacedKey BOSS_ID_KEY = new NamespacedKey("cobblemonboss", "bossid");
    private static final Map<String, Map<UUID, Double>> DAMAGE_MAP = new HashMap<>();
    private static final Map<String, List<String>> REWARD_CACHE = new HashMap<>();

    private BossManager getBossManager() {
        RegisteredServiceProvider<BossManager> rsp = Bukkit.getServicesManager().getRegistration(BossManager.class);
        return rsp != null ? rsp.getProvider() : null;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (!pdc.has(BOSS_ID_KEY, PersistentDataType.STRING)) return;
        String bossId = pdc.get(BOSS_ID_KEY, PersistentDataType.STRING);
        double damage = event.getFinalDamage();

        DAMAGE_MAP.computeIfAbsent(bossId, k -> new HashMap<>());
        Map<UUID, Double> bossDamage = DAMAGE_MAP.get(bossId);
        double total = bossDamage.getOrDefault(player.getUniqueId(), 0.0) + damage;
        bossDamage.put(player.getUniqueId(), total);

        BossManager manager = getBossManager();
        if (manager != null) {
            manager.updateAggro(bossId, player.getUniqueId(), (int) Math.round(total));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (!pdc.has(BOSS_ID_KEY, PersistentDataType.STRING)) return;
        String bossId = pdc.get(BOSS_ID_KEY, PersistentDataType.STRING);

        BossManager manager = getBossManager();
        if (manager != null) {
            manager.setAlive(bossId, false);
        }

        // 原地生成同种类野生宝可梦（保证最小V数）
        if (event.getEntity() instanceof PokemonEntity pokemonEntity) {
            Location loc = event.getEntity().getLocation();
            String species = pokemonEntity.getPokemon().getSpecies().getName();
            Pokemon pokemon = PokemonBuilder.builder()
                    .species(species)
                    .level(50)
                    .ivs(new IVs(31, 31, 31, 31, 31, 31))
                    .build();
            loc.getWorld().spawn(loc, PokemonEntity.class, e -> e.setPokemon(pokemon));
        }

        if (!DAMAGE_MAP.containsKey(bossId)) return;
        Map<UUID, Double> damageMap = DAMAGE_MAP.get(bossId);
        if (damageMap.isEmpty()) return;

        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(damageMap.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> rewards = getRewards(bossId);
        if (rewards == null || rewards.isEmpty()) return;

        for (int i = 0; i < sorted.size() && i < rewards.size(); i++) {
            UUID playerUUID = sorted.get(i).getKey();
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            String cmd = rewards.get(i).replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        DAMAGE_MAP.remove(bossId);
    }

    private List<String> getRewards(String bossId) {
        if (REWARD_CACHE.containsKey(bossId)) {
            return REWARD_CACHE.get(bossId);
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CobblemonBoss");
        if (plugin == null) return null;
        File file = new File(plugin.getDataFolder(), "bosses.yml");
        if (!file.exists()) return null;
        org.bukkit.configuration.file.YamlConfiguration cfg = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        List<String> rewards = cfg.getStringList(bossId + ".rewards");
        REWARD_CACHE.put(bossId, rewards);
        return rewards;
    }
}