import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.io.File;
import java.util.UUID;

public class RevenueTask extends BukkitRunnable {
    private final String clanId;

    public RevenueTask(String clanId) {
        this.clanId = clanId;
    }

    @Override
    public void run() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) return;

        File dataFile = new File(plugin.getDataFolder(), "clans.yml");
        if (!dataFile.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);
        String path = "clans." + clanId;
        if (!cfg.contains(path)) return;

        int level = cfg.getInt(path + ".level", 1);
        if (level < 1 || level > 6) {
            plugin.getLogger().warning("Invalid clan level for " + clanId + ": " + level);
            return;
        }

        String ownerUuidStr = cfg.getString(path + ".owner");
        if (ownerUuidStr == null) return;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("Vault economy not found, cannot pay clan revenue.");
            return;
        }

        Economy econ = rsp.getProvider();
        if (econ == null) return;

        long[] revenue = {500_000_000L, 1_000_000_000L, 2_000_000_000L, 4_000_000_000L, 5_000_000_000L, 6_000_000_000L};
        double amount = revenue[level - 1];

        UUID ownerUuid;
        try {
            ownerUuid = UUID.fromString(ownerUuidStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid owner UUID for clan " + clanId + ": " + ownerUuidStr);
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);
        EconomyResponse response = econ.depositPlayer(owner, amount);
        if (response.transactionSuccess()) {
            plugin.getLogger().info("Clan revenue of " + econ.format(amount) + " deposited to " + owner.getName() + " for clan " + clanId);
        } else {
            plugin.getLogger().warning("Failed to deposit clan revenue for " + clanId + ": " + response.errorMessage);
        }
    }
}