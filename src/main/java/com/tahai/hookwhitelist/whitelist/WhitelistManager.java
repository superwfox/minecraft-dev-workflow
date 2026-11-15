package com.tahai.hookwhitelist.whitelist;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {

    private final Plugin plugin;
    private final File dataFile;
    private final Map<UUID, String> entries = new LinkedHashMap<>();

    /**
     * 最近一次落盘是否真正成功。add/remove 依赖它决定是否向调用方回报成功，
     * 避免磁盘写入失败时仍提示“已加入/已移除白名单”。
     * 内存状态与磁盘内容始终保持一致：落盘失败即回滚内存，磁盘保持旧内容。
     */
    private volatile boolean lastSaveSucceeded = true;

    public WhitelistManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "whitelist.yml");
    }

    public void load() {
        entries.clear();
        lastSaveSucceeded = true;
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建插件数据目录: " + folder.getAbsolutePath());
            lastSaveSucceeded = false;
            return;
        }
        if (!dataFile.exists()) {
            // 文件不存在时创建空表
            save();
            return;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            // 读取失败时不落盘，避免把损坏的文件覆盖成空表导致白名单丢失
            plugin.getLogger().warning("读取 whitelist.yml 失败: " + e.getMessage());
            lastSaveSucceeded = false;
            return;
        }
        ConfigurationSection section = cfg.getConfigurationSection("players");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            String name = section.getString(key);
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                entries.put(UUID.fromString(key), name);
            } catch (IllegalArgumentException ignored) {
                // 非法 UUID 键忽略，不影响其它条目载入
            }
        }
    }

    public void save() {
        lastSaveSucceeded = persist();
    }

    /**
     * 将当前内存快照原子地写入 whitelist.yml，返回是否真正落盘成功。
     */
    private boolean persist() {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建插件数据目录，白名单未保存: " + folder.getAbsolutePath());
            return false;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<UUID, String> entry : entries.entrySet()) {
            cfg.set("players." + entry.getKey(), entry.getValue());
        }
        File tempFile = new File(folder, "whitelist.yml.tmp");
        try {
            cfg.save(tempFile);
            try {
                Files.move(tempFile.toPath(), dataFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(tempFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            if (tempFile.exists() && !tempFile.delete()) {
                plugin.getLogger().warning("临时文件清理失败: " + tempFile.getAbsolutePath());
            }
            plugin.getLogger().warning("保存 whitelist.yml 失败: " + e.getMessage());
            return false;
        }
    }

    public boolean contains(UUID uuid) {
        return uuid != null && entries.containsKey(uuid);
    }

    public boolean add(UUID uuid, String name) {
        if (uuid == null || name == null || name.isBlank()) {
            return false;
        }
        boolean existed = entries.containsKey(uuid);
        String previous = existed ? entries.get(uuid) : null;
        if (existed && name.equals(previous)) {
            // 数据未发生变化，无需重复写盘
            return false;
        }
        entries.put(uuid, name);
        if (!persist()) {
            // 落盘失败则回滚内存状态，保证内存与文件一致，并让调用方给出失败提示
            if (existed) {
                entries.put(uuid, previous);
            } else {
                entries.remove(uuid);
            }
            lastSaveSucceeded = false;
            plugin.getLogger().warning("白名单写入失败，已回滚: " + name + " (" + uuid + ")");
            return false;
        }
        lastSaveSucceeded = true;
        return true;
    }

    public boolean remove(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        if (!entries.containsKey(uuid)) {
            return false;
        }
        String previous = entries.remove(uuid);
        if (!persist()) {
            // 落盘失败则回滚内存状态，避免重启后白名单与提示不一致
            if (previous != null) {
                entries.put(uuid, previous);
            }
            lastSaveSucceeded = false;
            plugin.getLogger().warning("白名单移除落盘失败，已回滚: " + previous + " (" + uuid + ")");
            return false;
        }
        lastSaveSucceeded = true;
        return true;
    }

    public List<String> getNames() {
        return new ArrayList<>(entries.values());
    }
}