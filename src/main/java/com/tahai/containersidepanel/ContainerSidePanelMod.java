package com.tahai.containersidepanel;

import com.tahai.containersidepanel.config.ModConfig;
import com.tahai.containersidepanel.mapping.ContainerMergeManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ContainerSidePanelMod extends JavaPlugin {
    private ModConfig modConfig;
    private ContainerMergeManager containerMergeManager;

    @Override
    public void onEnable() {
        modConfig = new ModConfig();
        modConfig.init(this);

        containerMergeManager = new ContainerMergeManager();
    }

    @Override
    public void onDisable() {
        // 插件卸载时暂时无需额外清理
    }

    public ModConfig getModConfig() {
        return modConfig;
    }

    public ContainerMergeManager getContainerMergeManager() {
        return containerMergeManager;
    }
}