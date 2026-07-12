package com.tahai.customhud;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigLoader {
    private final String template;
    private final String horizontalPosition;
    private final String verticalPosition;
    private final String fontNamespace;

    public ConfigLoader(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        this.template = config.getString("hud-template");
        this.horizontalPosition = config.getString("hud-horizontal");
        this.verticalPosition = config.getString("hud-vertical");
        this.fontNamespace = config.getString("custom-font");
    }

    public String getTemplate() {
        return template;
    }

    public String getHorizontalPosition() {
        return horizontalPosition;
    }

    public String getVerticalPosition() {
        return verticalPosition;
    }

    public String getFontNamespace() {
        return fontNamespace;
    }
}