package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

public class VoteDataManager {

    private final File dataFile;

    public VoteDataManager() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Shenpan");
        if (plugin == null) {
            throw new IllegalStateException("Shenpan plugin not found");
        }
        this.dataFile = new File(plugin.getDataFolder(), "votes.txt");
        plugin.getDataFolder().mkdirs();
    }

    public void appendResult(VoteResult result) {
        String line = formatResult(result);
        try (FileWriter fw = new FileWriter(dataFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatResult(VoteResult result) {
        String candidates = result.voteCounts().entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
        return String.format("[%s] %s -> %s", result.timestamp(), candidates, result.outcome());
    }

    public record VoteResult(
            String timestamp,
            Map<String, Integer> voteCounts,
            String outcome
    ) {
        public VoteResult(Map<String, Integer> voteCounts, String outcome) {
            this(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    voteCounts, outcome);
        }
    }
}