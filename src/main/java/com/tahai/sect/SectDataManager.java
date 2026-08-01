package com.tahai.sect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SectDataManager {
    private final File dataFile;
    private final YamlConfiguration data;
    private final Map<String, Sect> sects = new HashMap<>();
    private War currentWar;

    public SectDataManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Sect");
        if (plugin == null) {
            throw new IllegalStateException("Sect plugin not found");
        }
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    private void load() {
        ConfigurationSection sectSection = data.getConfigurationSection("sects");
        if (sectSection != null) {
            for (String name : sectSection.getKeys(false)) {
                ConfigurationSection sec = sectSection.getConfigurationSection(name);
                if (sec == null) continue;
                String ownerStr = sec.getString("owner");
                if (ownerStr == null) continue;
                UUID owner;
                try {
                    owner = UUID.fromString(ownerStr);
                } catch (IllegalArgumentException e) {
                    continue;
                }
                int level = sec.getInt("level", 1);
                Map<UUID, String> members = new HashMap<>();
                ConfigurationSection memberSec = sec.getConfigurationSection("members");
                if (memberSec != null) {
                    for (String key : memberSec.getKeys(false)) {
                        try {
                            members.put(UUID.fromString(key), memberSec.getString(key, "MEMBER"));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
                List<String> invites = sec.getStringList("invites");
                sects.put(name, new Sect(name, owner, level, members, invites));
            }
        }
        ConfigurationSection warSec = data.getConfigurationSection("currentWar");
        if (warSec != null) {
            String attacker = warSec.getString("attacker");
            String defender = warSec.getString("defender");
            if (attacker != null && defender != null) {
                long startTime = warSec.getLong("startTime", System.currentTimeMillis());
                boolean accepted = warSec.getBoolean("accepted", false);
                currentWar = new War(attacker, defender, startTime);
                currentWar.accepted = accepted;
            }
        }
    }

    public synchronized void save() {
        data.set("sects", null);
        for (Map.Entry<String, Sect> entry : sects.entrySet()) {
            Sect sect = entry.getValue();
            String path = "sects." + entry.getKey();
            data.set(path + ".owner", sect.owner.toString());
            data.set(path + ".level", sect.level);
            for (Map.Entry<UUID, String> member : sect.members.entrySet()) {
                data.set(path + ".members." + member.getKey().toString(), member.getValue());
            }
            data.set(path + ".invites", new ArrayList<>(sect.invites));
        }
        if (currentWar != null) {
            data.set("currentWar.attacker", currentWar.attacker);
            data.set("currentWar.defender", currentWar.defender);
            data.set("currentWar.startTime", currentWar.startTime);
            data.set("currentWar.accepted", currentWar.accepted);
        } else {
            data.set("currentWar", null);
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save sect data", e);
        }
    }

    public void shutdown() {
        save();
    }

    public boolean createSect(String name, UUID owner) {
        if (sects.containsKey(name) || isInAnySect(owner)) return false;
        Sect sect = new Sect(name, owner, 1, new HashMap<>(), new ArrayList<>());
        sect.members.put(owner, "OWNER");
        sects.put(name, sect);
        save();
        return true;
    }

    public boolean invitePlayer(String sectName, UUID operator, UUID target) {
        Sect sect = sects.get(sectName);
        if (sect == null || !sect.owner.equals(operator)) return false;
        if (sect.members.containsKey(target) || sect.invites.contains(target.toString())) return false;
        if (isInAnySect(target)) return false;
        sect.invites.add(target.toString());
        save();
        return true;
    }

    public boolean acceptInvite(String sectName, UUID player) {
        Sect sect = sects.get(sectName);
        if (sect == null || !sect.invites.contains(player.toString())) return false;
        sect.invites.remove(player.toString());
        sect.members.put(player, "MEMBER");
        save();
        return true;
    }

    public boolean upgradeSect(String sectName, UUID operator) {
        Sect sect = sects.get(sectName);
        if (sect == null || !sect.owner.equals(operator)) return false;
        sect.level++;
        save();
        return true;
    }

    public boolean disbandSect(String sectName, UUID operator) {
        Sect sect = sects.get(sectName);
        if (sect == null || !sect.owner.equals(operator)) return false;
        sects.remove(sectName);
        if (currentWar != null && (currentWar.attacker.equals(sectName) || currentWar.defender.equals(sectName))) {
            currentWar = null;
        }
        save();
        return true;
    }

    public boolean startWar(String attackerSect, String defenderSect, UUID initiator) {
        if (currentWar != null) return false;
        Sect attacker = sects.get(attackerSect);
        Sect defender = sects.get(defenderSect);
        if (attacker == null || defender == null || !attacker.owner.equals(initiator)) return false;
        currentWar = new War(attackerSect, defenderSect, System.currentTimeMillis());
        save();
        return true;
    }

    public boolean acceptWar(String defenderSect, UUID defenderOwner) {
        if (currentWar == null || !currentWar.defender.equals(defenderSect)) return false;
        Sect defender = sects.get(defenderSect);
        if (defender == null || !defender.owner.equals(defenderOwner)) return false;
        currentWar.accepted = true;
        save();
        return true;
    }

    public boolean endWar(String sectName, UUID operator) {
        if (currentWar == null) return false;
        String otherSect = null;
        if (currentWar.attacker.equals(sectName)) {
            otherSect = currentWar.defender;
        } else if (currentWar.defender.equals(sectName)) {
            otherSect = currentWar.attacker;
        }
        if (otherSect == null) return false;
        Sect sect = sects.get(sectName);
        Sect other = sects.get(otherSect);
        if ((sect == null || !sect.owner.equals(operator)) && (other == null || !other.owner.equals(operator))) {
            return false;
        }
        currentWar = null;
        save();
        return true;
    }

    public boolean setRole(String sectName, UUID operator, UUID target, String role) {
        Sect sect = sects.get(sectName);
        if (sect == null || !sect.owner.equals(operator) || !sect.members.containsKey(target)) return false;
        sect.members.put(target, role);
        save();
        return true;
    }

    public boolean isInAnySect(UUID player) {
        for (Sect sect : sects.values()) {
            if (sect.members.containsKey(player)) return true;
        }
        return false;
    }

    public Sect getSect(String name) {
        return sects.get(name);
    }

    public Map<String, Sect> getSects() {
        return Collections.unmodifiableMap(sects);
    }

    public War getCurrentWar() {
        return currentWar;
    }

    public static class Sect {
        private final String name;
        private final UUID owner;
        private int level;
        private final Map<UUID, String> members;
        private final List<String> invites;

        private Sect(String name, UUID owner, int level, Map<UUID, String> members, List<String> invites) {
            this.name = name;
            this.owner = owner;
            this.level = level;
            this.members = members;
            this.invites = invites;
        }

        public String getName() { return name; }
        public UUID getOwner() { return owner; }
        public int getLevel() { return level; }
        public Map<UUID, String> getMembers() { return Collections.unmodifiableMap(members); }
        public List<String> getInvites() { return Collections.unmodifiableList(invites); }
    }

    public static class War {
        private final String attacker;
        private final String defender;
        private final long startTime;
        private boolean accepted;

        private War(String attacker, String defender, long startTime) {
            this.attacker = attacker;
            this.defender = defender;
            this.startTime = startTime;
        }

        public String getAttacker() { return attacker; }
        public String getDefender() { return defender; }
        public long getStartTime() { return startTime; }
        public boolean isAccepted() { return accepted; }
    }
}