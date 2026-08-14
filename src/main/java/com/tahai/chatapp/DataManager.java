import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DataManager {

    private static final int MAX_HISTORY_SIZE = 100;

    private final Plugin plugin;
    private final File dataFile;
    private YamlConfiguration config;

    private final Map<UUID, Set<UUID>> friends = new HashMap<>();
    private final Map<String, FriendRequest> pendingRequests = new LinkedHashMap<>();
    private final Map<Integer, GroupData> groups = new LinkedHashMap<>();
    private final Map<String, List<ChannelMessage>> historyMessages = new LinkedHashMap<>();
    private int nextGroupId = 1;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    private void load() {
        config = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection friendsSection = config.getConfigurationSection("friends");
        if (friendsSection != null) {
            for (String key : friendsSection.getKeys(false)) {
                UUID player = UUID.fromString(key);
                Set<UUID> playerFriends = new HashSet<>();
                for (String s : friendsSection.getStringList(key)) {
                    playerFriends.add(UUID.fromString(s));
                }
                friends.put(player, playerFriends);
            }
        }

        for (Map<?, ?> raw : config.getMapList("pendingRequests")) {
            UUID requester = UUID.fromString(String.valueOf(raw.get("requester")));
            UUID receiver = UUID.fromString(String.valueOf(raw.get("receiver")));
            String status = String.valueOf(raw.get("status"));
            FriendRequest req = new FriendRequest(requester, receiver, status);
            pendingRequests.put(pendingKey(requester, receiver), req);
        }

        ConfigurationSection groupsSection = config.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String key : groupsSection.getKeys(false)) {
                ConfigurationSection section = groupsSection.getConfigurationSection(key);
                int id = Integer.parseInt(key);
                String name = section.getString("name");
                UUID owner = UUID.fromString(section.getString("owner"));
                Set<UUID> admins = new HashSet<>();
                for (String s : section.getStringList("admins")) {
                    admins.add(UUID.fromString(s));
                }
                Set<UUID> members = new HashSet<>();
                for (String s : section.getStringList("members")) {
                    members.add(UUID.fromString(s));
                }
                Set<UUID> muted = new HashSet<>();
                for (String s : section.getStringList("muted")) {
                    muted.add(UUID.fromString(s));
                }
                boolean reviewEnabled = section.getBoolean("reviewEnabled", false);
                groups.put(id, new GroupData(id, name, owner, admins, members, muted, reviewEnabled));
                if (id >= nextGroupId) {
                    nextGroupId = id + 1;
                }
            }
        }

        ConfigurationSection historySection = config.getConfigurationSection("history");
        if (historySection != null) {
            for (String key : historySection.getKeys(false)) {
                List<ChannelMessage> messages = new ArrayList<>();
                for (Map<?, ?> raw : historySection.getMapList(key)) {
                    UUID sender = UUID.fromString(String.valueOf(raw.get("sender")));
                    long timestamp = Long.parseLong(String.valueOf(raw.get("timestamp")));
                    String content = String.valueOf(raw.get("content"));
                    messages.add(new ChannelMessage(sender, timestamp, content));
                }
                historyMessages.put(key, messages);
            }
        }
    }

    public void saveAll() {
        for (String key : config.getKeys(false)) {
            config.set(key, null);
        }

        ConfigurationSection friendsSection = config.createSection("friends");
        for (Map.Entry<UUID, Set<UUID>> entry : friends.entrySet()) {
            friendsSection.set(entry.getKey().toString(),
                    entry.getValue().stream().map(UUID::toString).toList());
        }

        List<Map<String, String>> requestList = new ArrayList<>();
        for (FriendRequest req : pendingRequests.values()) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("requester", req.requester.toString());
            map.put("receiver", req.receiver.toString());
            map.put("status", req.status);
            requestList.add(map);
        }
        config.set("pendingRequests", requestList);

        ConfigurationSection groupsSection = config.createSection("groups");
        for (GroupData g : groups.values()) {
            ConfigurationSection section = groupsSection.createSection(String.valueOf(g.id));
            section.set("name", g.name);
            section.set("owner", g.owner.toString());
            section.set("admins", g.admins.stream().map(UUID::toString).toList());
            section.set("members", g.members.stream().map(UUID::toString).toList());
            section.set("muted", g.muted.stream().map(UUID::toString).toList());
            section.set("reviewEnabled", g.reviewEnabled);
        }

        ConfigurationSection historySection = config.createSection("history");
        for (Map.Entry<String, List<ChannelMessage>> entry : historyMessages.entrySet()) {
            List<Map<String, Object>> msgList = new ArrayList<>();
            for (ChannelMessage msg : entry.getValue()) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sender", msg.sender.toString());
                map.put("timestamp", msg.timestamp);
                map.put("content", msg.content);
                msgList.add(map);
            }
            historySection.set(entry.getKey(), msgList);
        }

        try {
            config.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public Set<UUID> getFriends(UUID player) {
        Set<UUID> set = friends.get(player);
        return set == null ? Collections.emptySet() : Collections.unmodifiableSet(set);
    }

    public boolean areFriends(UUID player1, UUID player2) {
        Set<UUID> set = friends.get(player1);
        return set != null && set.contains(player2);
    }

    public void addFriend(UUID player1, UUID player2) {
        if (player1.equals(player2)) return;
        friends.computeIfAbsent(player1, k -> new HashSet<>()).add(player2);
        friends.computeIfAbsent(player2, k -> new HashSet<>()).add(player1);
        saveAll();
    }

    public void removeFriend(UUID player1, UUID player2) {
        Set<UUID> set1 = friends.get(player1);
        if (set1 != null && set1.remove(player2)) {
            Set<UUID> set2 = friends.get(player2);
            if (set2 != null) set2.remove(player1);
            saveAll();
        }
    }

    public boolean sendFriendRequest(UUID requester, UUID receiver) {
        if (requester.equals(receiver)) return false;
        if (areFriends(requester, receiver)) return false;
        String key = pendingKey(requester, receiver);
        if (pendingRequests.containsKey(key)) return false;
        pendingRequests.put(key, new FriendRequest(requester, receiver, "PENDING"));
        saveAll();
        return true;
    }

    public List<FriendRequest> getIncomingRequests(UUID player) {
        List<FriendRequest> result = new ArrayList<>();
        for (FriendRequest req : pendingRequests.values()) {
            if (req.receiver.equals(player) && "PENDING".equals(req.status)) {
                result.add(req);
            }
        }
        return result;
    }

    public List<FriendRequest> getOutgoingRequests(UUID player) {
        List<FriendRequest> result = new ArrayList<>();
        for (FriendRequest req : pendingRequests.values()) {
            if (req.requester.equals(player) && "PENDING".equals(req.status)) {
                result.add(req);
            }
        }
        return result;
    }

    public boolean acceptFriendRequest(UUID requester, UUID receiver) {
        String key = pendingKey(requester, receiver);
        if (pendingRequests.remove(key) == null) return false;
        addFriend(requester, receiver);
        return true;
    }

    public boolean declineFriendRequest(UUID requester, UUID receiver) {
        String key = pendingKey(requester, receiver);
        if (pendingRequests.remove(key) == null) return false;
        saveAll();
        return true;
    }

    public boolean hasPendingRequest(UUID requester, UUID receiver) {
        return pendingRequests.containsKey(pendingKey(requester, receiver));
    }

    public int createGroup(String name, UUID owner) {
        int id = nextGroupId++;
        Set<UUID> members = new HashSet<>();
        members.add(owner);
        groups.put(id, new GroupData(id, name, owner, new HashSet<>(), members, new HashSet<>(), false));
        saveAll();
        return id;
    }

    public boolean deleteGroup(int groupId) {
        if (groups.remove(groupId) == null) return false;
        historyMessages.remove("group:" + groupId);
        saveAll();
        return true;
    }

    public GroupData getGroup(int groupId) {
        return groups.get(groupId);
    }

    public Collection<GroupData> getAllGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public boolean addGroupMember(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        if (g == null || !g.members.add(player)) return false;
        saveAll();
        return true;
    }

    public boolean removeGroupMember(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        if (g == null) return false;
        boolean changed = g.members.remove(player);
        changed |= g.admins.remove(player);
        changed |= g.muted.remove(player);
        if (!changed) return false;
        saveAll();
        return true;
    }

    public boolean addGroupAdmin(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        if (g == null || !g.members.contains(player)) return false;
        if (!g.admins.add(player)) return false;
        saveAll();
        return true;
    }

    public boolean removeGroupAdmin(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        if (g == null || !g.admins.remove(player)) return false;
        saveAll();
        return true;
    }

    public boolean setGroupMuted(int groupId, UUID player, boolean muted) {
        GroupData g = groups.get(groupId);
        if (g == null) return false;
        boolean changed;
        if (muted) {
            changed = g.muted.add(player);
        } else {
            changed = g.muted.remove(player);
        }
        if (!changed) return false;
        saveAll();
        return true;
    }

    public boolean isGroupMember(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        return g != null && g.members.contains(player);
    }

    public boolean isGroupAdmin(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        return g != null && g.admins.contains(player);
    }

    public boolean isGroupMuted(int groupId, UUID player) {
        GroupData g = groups.get(groupId);
        return g != null && g.muted.contains(player);
    }

    public void setGroupReviewEnabled(int groupId, boolean enabled) {
        GroupData g = groups.get(groupId);
        if (g == null || g.reviewEnabled == enabled) return;
        g.reviewEnabled = enabled;
        saveAll();
    }

    public boolean isGroupReviewEnabled(int groupId) {
        GroupData g = groups.get(groupId);
        return g != null && g.reviewEnabled;
    }

    public void addChannelMessage(String channelKey, UUID sender, String content) {
        List<ChannelMessage> list = historyMessages.computeIfAbsent(channelKey, k -> new ArrayList<>());
        list.add(new ChannelMessage(sender, System.currentTimeMillis(), content));
        while (list.size() > MAX_HISTORY_SIZE) {
            list.remove(0);
        }
        saveAll();
    }

    public List<ChannelMessage> getChannelMessages(String channelKey) {
        List<ChannelMessage> list = historyMessages.get(channelKey);
        return list == null ? List.of() : List.copyOf(list);
    }

    public void addPublicMessage(UUID sender, String content) {
        addChannelMessage("public", sender, content);
    }

    public List<ChannelMessage> getPublicMessages() {
        return getChannelMessages("public");
    }

    public void addPrivateMessage(UUID player1, UUID player2, UUID sender, String content) {
        addChannelMessage(privateChannelKey(player1, player2), sender, content);
    }

    public List<ChannelMessage> getPrivateMessages(UUID player1, UUID player2) {
        return getChannelMessages(privateChannelKey(player1, player2));
    }

    public void addGroupMessage(int groupId, UUID sender, String content) {
        addChannelMessage("group:" + groupId, sender, content);
    }

    public List<ChannelMessage> getGroupMessages(int groupId) {
        return getChannelMessages("group:" + groupId);
    }

    public static String privateChannelKey(UUID p1, UUID p2) {
        String s1 = p1.toString();
        String s2 = p2.toString();
        return s1.compareTo(s2) <= 0 ? "private:" + s1 + ":" + s2 : "private:" + s2 + ":" + s1;
    }

    private static String pendingKey(UUID requester, UUID receiver) {
        return requester.toString() + ":" + receiver.toString();
    }

    public static class FriendRequest {
        private final UUID requester;
        private final UUID receiver;
        private final String status;

        private FriendRequest(UUID requester, UUID receiver, String status) {
            this.requester = requester;
            this.receiver = receiver;
            this.status = status;
        }

        public UUID getRequester() {
            return requester;
        }

        public UUID getReceiver() {
            return receiver;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class GroupData {
        private final int id;
        private final String name;
        private final UUID owner;
        private final Set<UUID> admins;
        private final Set<UUID> members;
        private final Set<UUID> muted;
        private boolean reviewEnabled;

        private GroupData(int id, String name, UUID owner, Set<UUID> admins,
                          Set<UUID> members, Set<UUID> muted, boolean reviewEnabled) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.admins = admins;
            this.members = members;
            this.muted = muted;
            this.reviewEnabled = reviewEnabled;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public UUID getOwner() {
            return owner;
        }

        public Set<UUID> getAdmins() {
            return Collections.unmodifiableSet(admins);
        }

        public Set<UUID> getMembers() {
            return Collections.unmodifiableSet(members);
        }

        public Set<UUID> getMuted() {
            return Collections.unmodifiableSet(muted);
        }

        public boolean isReviewEnabled() {
            return reviewEnabled;
        }
    }

    public static class ChannelMessage {
        private final UUID sender;
        private final long timestamp;
        private final String content;

        private ChannelMessage(UUID sender, long timestamp, String content) {
            this.sender = sender;
            this.timestamp = timestamp;
            this.content = content;
        }

        public UUID getSender() {
            return sender;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getContent() {
            return content;
        }
    }
}