package com.tahai.banitem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class DatabaseManager {
    private final Plugin plugin;
    private Connection connection;
    private final Set<String> itemsCache = new HashSet<>();

    public DatabaseManager(Plugin plugin) {
        this.plugin = plugin;
        loadDatabase();
        loadCache();
    }

    private void loadDatabase() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        String database = plugin.getConfig().getString("mysql.database", "minecraft");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "");

        try {
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                username,
                password
            );
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS banned_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "item_data TEXT NOT NULL)"
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to MySQL database", e);
            connection = null;
        }
    }

    private void loadCache() {
        if (connection == null) return;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT item_data FROM banned_items")) {
            while (resultSet.next()) {
                itemsCache.add(resultSet.getString("item_data"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load banned items from database", e);
        }
    }

    public boolean isBanned(ItemStack item) {
        return itemsCache.contains(serialize(item));
    }

    public boolean addBanned(ItemStack item) {
        String data = serialize(item);
        if (itemsCache.contains(data)) return false;
        if (connection == null) return false;
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO banned_items (item_data) VALUES (?)")) {
            statement.setString(1, data);
            statement.executeUpdate();
            itemsCache.add(data);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to add banned item", e);
            return false;
        }
    }

    public boolean removeBanned(ItemStack item) {
        String data = serialize(item);
        if (!itemsCache.contains(data)) return false;
        if (connection == null) return false;
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM banned_items WHERE item_data = ?")) {
            statement.setString(1, data);
            statement.executeUpdate();
            itemsCache.remove(data);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to remove banned item", e);
            return false;
        }
    }

    public Set<String> getBannedItems() {
        return itemsCache;
    }

    public String serialize(ItemStack item) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(item);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    public ItemStack deserialize(String data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (ItemStack) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize ItemStack", e);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection", e);
            }
            connection = null;
        }
    }
}