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
     * 是否已成功从磁盘载入（或确认文件不存在并创建了空表）。
     * 载入失败（文件损坏/不可读/数据目录不可创建）时保持 false，
     * 此时 save() 与 add/remove 一律拒绝落盘，避免用内存中的空表覆盖原 whitelist.yml 造成白名单丢失。
     */
    private volatile boolean loaded = false;

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
        loaded = false;
        lastSaveSucceeded = true;
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("无法创建插件数据目录，跳过白名单载入: " + folder.getAbsolutePath());
            return;
        }
        if (!dataFile.exists()) {
            // 文件不存在时创建空表
            entries.clear();
            loaded = true;
            save();
            return;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(dataFile);
        } catch (IOException | InvalidConfigurationException e) {
            // 读取失败时保持 loaded=false：不修改内存表，也不会在 onDisable 时把空表写回覆盖原文件
            plugin.getLogger().warning("读取 whitelist.yml 失败，已停用白名单写入以保护原文件: " + e.getMessage());
            lastSaveSucceeded = false;
            return;
        }
        // 先解析到临时表，全部成功后再替换内存表，避免载入中途异常留下半份数据
        Map<UUID, String> parsed = new LinkedHashMap<>();
        ConfigurationSection section = cfg.getConfigurationSection("players");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String name = section.getString(key);
                if (name == null || name.isBlank()) {
                    continue;
                }
                try {
                    parsed.put(UUID.fromString(key), name);
                } catch (IllegalArgumentException ignored) {
                    // 非法 UUID 键忽略，不影响其它条目载入
                }
            }
        }
        entries.clear();
        entries.putAll(parsed);
        loaded = true;
    }

    public void save() {
        if (!loaded) {
            // 未成功载入时禁止写盘，防止把空表覆盖到仍存在于磁盘上的 whitelist.yml
            lastSaveSucceeded = false;
            plugin.getLogger().warning("白名单尚未成功载入，已跳过保存以避免覆盖 whitelist.yml");
            return;
        }
        lastSaveSucceeded = persist();
    }

    /**
     * 将当前内存快照原子地写入 whitelist.yml，返回是否真正落盘成功。
     */
    private boolean persist() {
        if (!loaded) {
            return false;
        }
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
        if (!loaded) {
            // 载入失败时拒绝修改，避免仅凭内存中的残缺数据整体覆写磁盘文件
            lastSaveSucceeded = false;
            plugin.getLogger().warning("白名单尚未成功载入，拒绝写入: " + name + " (" + uuid + ")");
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
        if (!loaded) {
            lastSaveSucceeded = false;
            plugin.getLogger().warning("白名单尚未成功载入，拒绝移除: " + uuid);
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