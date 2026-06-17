package com.tahai.shenpan;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShenPanCommand implements CommandExecutor, TabCompleter {

    private static VoteManager voteManager;
    private final Plugin plugin;

    public ShenPanCommand() {
        this.plugin = Bukkit.getPluginManager().getPlugin("Shenpan");
    }

    private static VoteManager getVoteManager() {
        if (voteManager == null) {
            voteManager = new VoteManager();
        }
        return voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(getMessage("messages.no-permission"));
            return true;
        }

        if (getVoteManager().isVoting()) {
            sender.sendMessage(getMessage("messages.vote-already"));
            return true;
        }

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.size() < 2) {
            sender.sendMessage(getMessage("messages.player-not-found"));
            return true;
        }

        Collections.shuffle(onlinePlayers);
        List<Player> candidates = onlinePlayers.subList(0, 2);
        List<String> candidateNames = candidates.stream().map(Player::getName).collect(Collectors.toList());

        boolean success = getVoteManager().createVote(candidateNames);
        if (!success) {
            sender.sendMessage(getMessage("messages.vote-already"));
            return true;
        }

        String prefix = getConfigString("messages.prefix");
        String voteStartMsg = getConfigString("messages.vote-start")
                .replace("%candidate1%", candidateNames.get(0))
                .replace("%candidate2%", candidateNames.get(1));

        Component message = Component.text(prefix + voteStartMsg, NamedTextColor.GOLD)
                .append(Component.text(" [" + candidateNames.get(0) + "]", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/vote " + candidateNames.get(0))))
                .append(Component.text(" [" + candidateNames.get(1) + "]", NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/vote " + candidateNames.get(1))));

        Bukkit.getServer().broadcast(message);
        sender.sendMessage(getMessage("messages.vote-success"));
        return true;
    }

    private String getMessage(String path) {
        return getConfigString("messages.prefix") + getConfigString(path);
    }

    private String getConfigString(String path) {
        return plugin.getConfig().getString(path, "");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}