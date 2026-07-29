package com.tahai.mahjong;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class GameManager {

    private final PlayerDataManager playerDataManager;
    private final Map<String, Table> tables = new HashMap<>();
    private int nextTableId = 1;

    public GameManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public String createTable(Player owner, GameMode mode) {
        String id = "table-" + (nextTableId++);
        Table table = new Table(id, mode);
        table.players.add(owner);
        tables.put(id, table);
        owner.sendMessage(ChatColor.GRAY + "你创建了牌桌 " + ChatColor.YELLOW + id + ChatColor.GRAY + " (" + mode + " 规则)。");
        return id;
    }

    public boolean joinTable(String tableId, Player player) {
        Table table = tables.get(tableId);
        if (table == null) {
            player.sendMessage(ChatColor.AQUA + "牌桌不存在。");
            return false;
        }
        if (table.players.size() >= 4) {
            player.sendMessage(ChatColor.AQUA + "牌桌已满。");
            return false;
        }
        if (table.started) {
            player.sendMessage(ChatColor.AQUA + "游戏已经开始。");
            return false;
        }
        if (table.players.contains(player)) {
            player.sendMessage(ChatColor.AQUA + "你已经在牌桌中。");
            return false;
        }
        table.players.add(player);
        table.ready.put(player.getUniqueId(), false);
        broadcast(table, ChatColor.GRAY + "玩家 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + "加入了牌桌。");
        return true;
    }

    public boolean leaveTable(String tableId, Player player) {
        Table table = tables.get(tableId);
        if (table == null) return false;
        if (!table.players.contains(player)) return false;
        if (table.started) {
            player.sendMessage(ChatColor.AQUA + "游戏进行中，不能离开。");
            return false;
        }
        table.players.remove(player);
        table.ready.remove(player.getUniqueId());
        broadcast(table, ChatColor.GRAY + "玩家 " + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + "离开了牌桌。");
        if (table.players.isEmpty()) {
            tables.remove(tableId);
        }
        return true;
    }

    public boolean setReady(String tableId, Player player) {
        Table table = tables.get(tableId);
        if (table == null) return false;
        if (!table.players.contains(player)) return false;
        if (table.started) return false;
        table.ready.put(player.getUniqueId(), true);
        player.sendMessage(ChatColor.GRAY + "你已准备。");
        return true;
    }

    public boolean startGame(String tableId) {
        Table table = tables.get(tableId);
        if (table == null) return false;
        if (table.started) return false;
        for (Player p : table.players) {
            if (!table.ready.getOrDefault(p.getUniqueId(), false)) {
                broadcast(table, ChatColor.AQUA + "请等待所有玩家准备。");
                return false;
            }
        }
        if (table.players.size() < 2) {
            broadcast(table, ChatColor.AQUA + "至少需要2名玩家。");
            return false;
        }
        table.start();
        broadcast(table, ChatColor.YELLOW + "游戏开始！");
        return true;
    }

    public boolean playerPlayTile(String tableId, Player player, String tile) {
        Table table = tables.get(tableId);
        if (table == null) return false;
        if (!table.started || table.finished) return false;
        if (table.currentPlayer == null || !table.currentPlayer.equals(player)) return false;
        if (!table.seats.get(player.getUniqueId()).hand.remove(tile)) {
            player.sendMessage(ChatColor.AQUA + "你没有这张牌。");
            return false;
        }
        table.lastTile = tile;
        table.discardPile.add(tile);
        broadcast(table, ChatColor.GRAY + player.getName() + " 打出了 " + ChatColor.YELLOW + tile);
        table.checkAfterDiscard();
        table.nextTurn();
        return true;
    }

    public boolean playerAction(String tableId, Player player, String action, String tile) {
        Table table = tables.get(tableId);
        if (table == null) return false;
        if (!table.started || table.finished) return false;
        if (action.equals("pong")) {
            return table.pong(player, tile);
        } else if (action.equals("kong")) {
            return table.kong(player, tile);
        } else if (action.equals("hu")) {
            return table.hu(player, tile);
        }
        return false;
    }

    public String getTableStatus(String tableId) {
        Table table = tables.get(tableId);
        if (table == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GRAY).append("牌桌 ").append(ChatColor.YELLOW).append(tableId).append(ChatColor.GRAY).append("状态：");
        sb.append("\n").append(ChatColor.GRAY).append("规则：").append(ChatColor.YELLOW).append(table.mode);
        sb.append("\n").append(ChatColor.GRAY).append("阶段：").append(ChatColor.YELLOW).append(table.started ? (table.finished ? "已结束" : "进行中") : "等待开始");
        sb.append("\n").append(ChatColor.GRAY).append("玩家：");
        for (Player p : table.players) {
            sb.append("\n  ").append(ChatColor.YELLOW).append(p.getName());
            if (table.seats.containsKey(p.getUniqueId())) {
                sb.append(" (手牌: ").append(table.seats.get(p.getUniqueId()).hand.size()).append("张)");
            }
        }
        sb.append("\n").append(ChatColor.GRAY).append("当前玩家：").append(table.currentPlayer == null ? "无" : table.currentPlayer.getName());
        sb.append("\n").append(ChatColor.GRAY).append("牌河 (最近): ");
        int len = table.discardPile.size();
        for (int i = Math.max(0, len - 5); i < len; i++) {
            sb.append(table.discardPile.get(i)).append(" ");
        }
        return sb.toString();
    }

    public List<String> getTableList() {
        return new ArrayList<>(tables.keySet());
    }

    private void broadcast(Table table, String message) {
        for (Player p : table.players) {
            p.sendMessage(message);
        }
    }

    public enum GameMode {
        STANDARD, SICHUAN
    }

    public class Table {
        final String id;
        final GameMode mode;
        final List<Player> players = new ArrayList<>();
        final Map<UUID, Seat> seats = new HashMap<>();
        final Map<UUID, Boolean> ready = new HashMap<>();
        final List<String> wall = new ArrayList<>();
        final List<String> discardPile = new ArrayList<>();
        Player currentPlayer;
        String lastTile;
        boolean started;
        boolean finished;
        int currentPlayerIndex;

        Table(String id, GameMode mode) {
            this.id = id;
            this.mode = mode;
        }

        void start() {
            started = true;
            List<String> tiles = new ArrayList<>();
            for (String suit : new String[]{"万", "条", "筒"}) {
                for (int i = 1; i <= 9; i++) {
                    for (int j = 0; j < 4; j++) {
                        tiles.add(i + suit);
                    }
                }
            }
            for (String honor : new String[]{"东", "南", "西", "北", "中", "发", "白"}) {
                for (int j = 0; j < 4; j++) {
                    tiles.add(honor);
                }
            }
            Collections.shuffle(tiles);
            wall.addAll(tiles);

            for (Player p : players) {
                Seat seat = new Seat();
                for (int i = 0; i < 13; i++) {
                    seat.hand.add(wall.remove(0));
                }
                seats.put(p.getUniqueId(), seat);
            }
            currentPlayerIndex = new Random().nextInt(players.size());
            currentPlayer = players.get(currentPlayerIndex);
            drawTile();
        }

        private void drawTile() {
            if (wall.isEmpty()) {
                finished = true;
                broadcast(this, ChatColor.AQUA + "流局！");
                return;
            }
            String drawn = wall.remove(0);
            seats.get(currentPlayer.getUniqueId()).hand.add(drawn);
            currentPlayer.sendMessage(ChatColor.GRAY + "你摸到了一张牌。当前手牌: " + ChatColor.YELLOW + String.join(" ", seats.get(currentPlayer.getUniqueId()).hand));
        }

        private void nextTurn() {
            if (finished) return;
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            currentPlayer = players.get(currentPlayerIndex);
            if (isRobot(currentPlayer)) {
                Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MahjongPlugin"), () -> {
                    if (finished || !started) return;
                    Seat seat = seats.get(currentPlayer.getUniqueId());
                    AiDecision decision = AiUtil.decide(
                            new ArrayList<>(seat.hand),
                            new ArrayList<>(seat.melds),
                            new ArrayList<>(discardPile),
                            lastTile,
                            AiUtil.AiLevel.EASY
                    );
                    String actionStr = decision.getAction().name().toLowerCase();
                    if (actionStr.equals("discard")) {
                        playerPlayTile(id, currentPlayer, decision.getTile());
                    } else if (actionStr.equals("pong") || actionStr.equals("kong") || actionStr.equals("hu")) {
                        playerAction(id, currentPlayer, actionStr, decision.getTile());
                    }
                }, 20L);
            } else {
                currentPlayer.sendMessage(ChatColor.GRAY + "轮到你了。输入 " + ChatColor.YELLOW + "/mj play <牌> " + ChatColor.GRAY + "出牌。");
            }
        }

        void checkAfterDiscard() {
            for (int i = 1; i < players.size(); i++) {
                int idx = (currentPlayerIndex + i) % players.size();
                Player p = players.get(idx);
                Seat seat = seats.get(p.getUniqueId());
                if (canHu(seat, lastTile)) {
                    if (isRobot(p)) {
                        hu(p, lastTile);
                        return;
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "可以胡牌！输入 /mj action hu " + lastTile);
                    }
                }
            }
            for (Player p : players) {
                if (p.equals(currentPlayer)) continue;
                Seat seat = seats.get(p.getUniqueId());
                if (canPong(seat, lastTile)) {
                    if (isRobot(p)) {
                        pong(p, lastTile);
                        return;
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "你可以碰牌！输入 /mj action pong " + lastTile);
                    }
                }
                if (canKong(seat, lastTile)) {
                    if (isRobot(p)) {
                        kong(p, lastTile);
                        return;
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "你可以杠牌！输入 /mj action kong " + lastTile);
                    }
                }
            }
        }

        private boolean canHu(Seat seat, String tile) {
            return false;
        }

        private boolean canPong(Seat seat, String tile) {
            int count = (int) seat.hand.stream().filter(t -> t.equals(tile)).count();
            return count >= 2;
        }

        private boolean canKong(Seat seat, String tile) {
            int count = (int) seat.hand.stream().filter(t -> t.equals(tile)).count();
            return count == 3;
        }

        boolean pong(Player player, String tile) {
            Seat seat = seats.get(player.getUniqueId());
            if (!canPong(seat, tile)) return false;
            seat.hand.remove(tile);
            seat.hand.remove(tile);
            seat.melds.add("碰" + tile);
            seat.melds.add("碰" + tile);
            seat.melds.add("碰" + tile);
            broadcast(this, ChatColor.GRAY + player.getName() + " 碰了 " + ChatColor.YELLOW + tile);
            currentPlayer = player;
            return true;
        }

        boolean kong(Player player, String tile) {
            Seat seat = seats.get(player.getUniqueId());
            if (!canKong(seat, tile)) return false;
            seat.hand.remove(tile);
            seat.hand.remove(tile);
            seat.hand.remove(tile);
            seat.melds.add("杠" + tile);
            seat.melds.add("杠" + tile);
            seat.melds.add("杠" + tile);
            seat.melds.add("杠" + tile);
            broadcast(this, ChatColor.GRAY + player.getName() + " 杠了 " + ChatColor.YELLOW + tile);
            drawTile();
            return true;
        }

        boolean hu(Player player, String tile) {
            Seat seat = seats.get(player.getUniqueId());
            finished = true;
            broadcast(this, ChatColor.YELLOW + player.getName() + " 胡牌！");
            if (tile != null && !tile.isEmpty()) {
                Player discarder = currentPlayer;
                int baseScore = 2;
                playerDataManager.addScore(player, baseScore);
                playerDataManager.subtractScore(discarder, baseScore);
                broadcast(this, ChatColor.GRAY + player.getName() + " +" + baseScore + " 分, " + discarder.getName() + " -" + baseScore + " 分");
            } else {
                for (Player p : players) {
                    if (p.equals(player)) continue;
                    playerDataManager.addScore(player, 1);
                    playerDataManager.subtractScore(p, 1);
                    broadcast(this, ChatColor.GRAY + player.getName() + " +1 分, " + p.getName() + " -1 分");
                }
            }
            return true;
        }

        private boolean isRobot(Player player) {
            return player.getName().contains("Bot") || player.getName().contains("AI");
        }

        class Seat {
            final List<String> hand = new ArrayList<>();
            final List<String> melds = new ArrayList<>();
        }
    }
}