package com.tahai.unpc;

import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class NpcManager {

    private final Plugin plugin;
    private final Map<Integer, NpcData> npcDataMap = new TreeMap<>();
    private final Map<Integer, ArmorStand> armorStands = new TreeMap<>();
    private final Map<Integer, TextDisplay> displays = new TreeMap<>();

    public NpcManager() {
        this.plugin = Bukkit.getPluginManager().getPlugin("Unpc");
        if (this.plugin == null) {
            throw new IllegalStateException("Unpc plugin not found");
        }
        reload();
    }

    public NpcData create(Location location, String displayText, String skinPlayerName,
                          String skinTexture, String skinSignature, String command, String commandExecutor) {
        int maxId = 0;
        for (int id : npcDataMap.keySet()) {
            if (id > maxId) maxId = id;
        }
        NpcData data = new NpcData(maxId + 1, location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(),
                displayText, skinPlayerName, skinTexture, skinSignature,
                command, commandExecutor);
        npcDataMap.put(data.getId(), data);
        spawnEntities(data);
        save();
        return data;
    }

    public void setDisplay(int id, String displayText) {
        NpcData data = npcDataMap.get(id);
        if (data == null) return;
        data.setDisplayText(displayText);
        TextDisplay display = displays.get(id);
        if (display != null) {
            display.setText(displayText == null ? "" : displayText);
        }
        save();
    }

    public void setSkin(int id, String skinPlayerName, String skinTexture, String skinSignature) {
        NpcData data = npcDataMap.get(id);
        if (data == null) return;
        data.setSkinPlayerName(skinPlayerName);
        data.setSkinTexture(skinTexture);
        data.setSkinSignature(skinSignature);
        ArmorStand stand = armorStands.get(id);
        if (stand != null) applySkin(stand, data);
        save();
    }

    public void setCommand(int id, String command, String commandExecutor) {
        NpcData data = npcDataMap.get(id);
        if (data == null) return;
        data.setCommand(command);
        data.setCommandExecutor(commandExecutor);
        save();
    }

    public void delete(int id) {
        if (npcDataMap.remove(id) == null) return;
        removeEntities(id);
        save();
    }

    public void reload() {
        removeAllEntities();
        npcDataMap.clear();
        loadNpcs();
    }

    public void save() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (NpcData data : npcDataMap.values()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", data.getId());
            map.put("world", data.getWorldName());
            map.put("x", data.getX());
            map.put("y", data.getY());
            map.put("z", data.getZ());
            map.put("yaw", data.getYaw());
            map.put("pitch", data.getPitch());
            map.put("displayText", data.getDisplayText());
            map.put("skinPlayerName", data.getSkinPlayerName());
            map.put("skinTexture", data.getSkinTexture());
            map.put("skinSignature", data.getSkinSignature());
            map.put("command", data.getCommand());
            map.put("commandExecutor", data.getCommandExecutor());
            list.add(map);
        }
        plugin.getConfig().set("npcs", list);
        plugin.saveConfig();
    }

    private void loadNpcs() {
        List<?> list = plugin.getConfig().getList("npcs");
        if (list == null) return;
        for (Object obj : list) {
            if (!(obj instanceof Map<?, ?>)) continue;
            Map<?, ?> map = (Map<?, ?>) obj;
            int id = ((Number) map.get("id")).intValue();
            String worldName = (String) map.get("world");
            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            float yaw = ((Number) map.get("yaw")).floatValue();
            float pitch = ((Number) map.get("pitch")).floatValue();
            String displayText = map.get("displayText") == null ? "" : (String) map.get("displayText");
            String skinPlayerName = map.get("skinPlayerName") == null ? "" : (String) map.get("skinPlayerName");
            String skinTexture = map.get("skinTexture") == null ? "" : (String) map.get("skinTexture");
            String skinSignature = map.get("skinSignature") == null ? "" : (String) map.get("skinSignature");
            String command = map.get("command") == null ? "" : (String) map.get("command");
            String commandExecutor = map.get("commandExecutor") == null ? "player" : (String) map.get("commandExecutor");
            NpcData data = new NpcData(id, worldName, x, y, z, yaw, pitch,
                    displayText, skinPlayerName, skinTexture, skinSignature, command, commandExecutor);
            npcDataMap.put(id, data);
            spawnEntities(data);
        }
    }

    private void spawnEntities(NpcData data) {
        World world = Bukkit.getWorld(data.getWorldName());
        if (world == null) {
            plugin.getLogger().warning("NPC #" + data.getId() + " skipped: world '" + data.getWorldName() + "' not found");
            return;
        }
        Location loc = new Location(world, data.getX(), data.getY(), data.getZ(), data.getYaw(), data.getPitch());

        ArmorStand stand = (ArmorStand) world.spawn(loc, ArmorStand.class);
        stand.setInvisible(true);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setGravity(false);
        stand.setCanPickupItems(false);
        stand.setInvulnerable(true);
        stand.setPersistent(true);
        applySkin(stand, data);
        armorStands.put(data.getId(), stand);

        Location textLoc = loc.clone().add(0, 0.75, 0);
        TextDisplay display = (TextDisplay) world.spawn(textLoc, TextDisplay.class);
        display.setText(data.getDisplayText() == null ? "" : data.getDisplayText());
        display.setBillboard(Display.Billboard.CENTER);
        display.setGravity(false);
        display.setSeeThrough(true);
        display.setDefaultBackground(false);
        display.setShadowed(false);
        display.setPersistent(true);
        displays.put(data.getId(), display);
    }

    private void applySkin(ArmorStand stand, NpcData data) {
        if (stand == null) return;
        String name = data.getSkinPlayerName() == null || data.getSkinPlayerName().isEmpty() ? "Steve" : data.getSkinPlayerName();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        org.bukkit.profile.PlayerProfile profile;
        String texture = data.getSkinTexture();
        String signature = data.getSkinSignature();
        if (texture != null && !texture.isEmpty() && signature != null && !signature.isEmpty()) {
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            profile = Bukkit.getServer().createProfile(uuid, name);
            profile.getProperties().add(new ProfileProperty("textures", texture, signature));
        } else {
            profile = Bukkit.getOfflinePlayer(name).getPlayerProfile();
        }

        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        stand.getEquipment().setHelmet(head);
    }

    private void removeEntities(int id) {
        ArmorStand stand = armorStands.remove(id);
        if (stand != null && stand.isValid()) stand.remove();
        TextDisplay display = displays.remove(id);
        if (display != null && display.isValid()) display.remove();
    }

    private void removeAllEntities() {
        for (ArmorStand stand : armorStands.values()) {
            if (stand != null && stand.isValid()) stand.remove();
        }
        for (TextDisplay display : displays.values()) {
            if (display != null && display.isValid()) display.remove();
        }
        armorStands.clear();
        displays.clear();
    }
}