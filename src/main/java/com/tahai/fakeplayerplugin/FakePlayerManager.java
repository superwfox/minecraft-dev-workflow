package com.tahai.fakeplayerplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FakePlayerManager {

    private final Map<String, FakePlayer> fakePlayers = new HashMap<>();

    public void save() {
    }

    public void shutdown() {
        for (String name : new ArrayList<>(fakePlayers.keySet())) {
            removeFakePlayer(name);
        }
    }

    public boolean createFakePlayer(String name, Location location) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(location, "location");
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }

        removeFakePlayer(name);

        UUID uuid = UUID.nameUUIDFromBytes(("FakePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Object entity = null;
        try {
            entity = createEntity(name, uuid, location);
            sendPacketToAll(newPlayerInfo(entity, "ADD_PLAYER"));
            sendPacketToAll(newSpawnPacket(entity, uuid, location));

            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            Plugin plugin = plugin();
            world.addPluginChunkTicket(chunkX, chunkZ, plugin);

            fakePlayers.put(name, new FakePlayer(name, uuid, entity, world, chunkX, chunkZ, plugin));
            return true;
        } catch (ReflectiveOperationException e) {
            if (entity != null) {
                try {
                    sendPacketToAll(newPlayerInfoRemove(entity, uuid));
                    sendPacketToAll(newDestroyPacket(entity));
                } catch (ReflectiveOperationException ignored) {
                }
            }
            throw new RuntimeException("Failed to create fake player " + name, e);
        }
    }

    public boolean removeFakePlayer(String name) {
        FakePlayer fake = fakePlayers.remove(name);
        if (fake == null) {
            return false;
        }

        try {
            sendPacketToAll(newPlayerInfoRemove(fake.entity, fake.uuid));
            sendPacketToAll(newDestroyPacket(fake.entity));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to remove fake player " + name, e);
        } finally {
            fake.world.removePluginChunkTicket(fake.chunkX, fake.chunkZ, fake.plugin);
        }

        return true;
    }

    private Plugin plugin() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("FakePlayer");
        if (plugin == null) {
            throw new IllegalStateException("FakePlayer plugin not loaded");
        }
        return plugin;
    }

    private Object createEntity(String name, UUID uuid, Location location) throws ReflectiveOperationException {
        Object server = getNmsServer();
        Object level = getNmsWorld(location.getWorld());

        Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
        Object profile = gameProfileClass.getConstructor(UUID.class, String.class).newInstance(uuid, name);

        Class<?> serverPlayerClass = nmsClass(
                "net.minecraft.server.level.ServerPlayer",
                "net.minecraft.server.level.EntityPlayer"
        );

        Object[] args = {server, level, profile};
        for (Constructor<?> ctor : serverPlayerClass.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 3
                    && params[0].isInstance(server)
                    && params[1].isInstance(level)
                    && params[2].isInstance(profile)) {
                Object entity = ctor.newInstance(args);
                setEntityLocation(entity, location);
                return entity;
            }
        }

        Object clientInfo = null;
        try {
            Class<?> clientInfoClass = Class.forName("net.minecraft.world.entity.player.ClientInformation");
            Method createDefault = clientInfoClass.getMethod("createDefault");
            clientInfo = createDefault.invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException ignore) {
        }

        if (clientInfo != null) {
            Object[] fourArgs = {server, level, profile, clientInfo};
            for (Constructor<?> ctor : serverPlayerClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 4
                        && params[0].isInstance(server)
                        && params[1].isInstance(level)
                        && params[2].isInstance(profile)
                        && params[3].isInstance(clientInfo)) {
                    Object entity = ctor.newInstance(fourArgs);
                    setEntityLocation(entity, location);
                    return entity;
                }
            }
        }

        throw new NoSuchMethodException("ServerPlayer constructor not found");
    }

    private Object newPlayerInfo(Object entity, String actionName) throws ReflectiveOperationException {
        Class<?> packetClass = nmsClass(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket",
                "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo"
        );

        Class<?> actionClass = null;
        for (Class<?> declared : packetClass.getDeclaredClasses()) {
            if (declared.isEnum() && declared.getSimpleName().endsWith("Action")) {
                actionClass = declared;
                break;
            }
        }
        if (actionClass == null) {
            throw new NoSuchMethodException("PlayerInfo action enum not found");
        }

        Object action = Enum.valueOf((Class<? extends Enum>) actionClass, actionName);

        for (Constructor<?> ctor : packetClass.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length != 2 || !params[0].isInstance(action)) {
                continue;
            }
            if (params[1].isInstance(entity)) {
                return ctor.newInstance(action, entity);
            }
            if (params[1].isArray() && params[1].getComponentType().isInstance(entity)) {
                Object array = Array.newInstance(params[1].getComponentType(), 1);
                Array.set(array, 0, entity);
                return ctor.newInstance(new Object[]{action, array});
            }
        }

        throw new NoSuchMethodException("PlayerInfo constructor not found");
    }

    private Object newPlayerInfoRemove(Object entity, UUID uuid) throws ReflectiveOperationException {
        try {
            Class<?> removeClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");
            for (Constructor<?> ctor : removeClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length != 1) {
                    continue;
                }
                if (params[0].isAssignableFrom(UUID.class)) {
                    return ctor.newInstance(uuid);
                }
                if (Collection.class.isAssignableFrom(params[0])) {
                    return ctor.newInstance(Collections.singletonList(uuid));
                }
                if (params[0].isArray() && params[0].getComponentType().isAssignableFrom(UUID.class)) {
                    Object array = Array.newInstance(params[0].getComponentType(), 1);
                    Array.set(array, 0, uuid);
                    return ctor.newInstance(array);
                }
            }
        } catch (ClassNotFoundException ignored) {
            return newPlayerInfo(entity, "REMOVE_PLAYER");
        }

        throw new NoSuchMethodException("ClientboundPlayerInfoRemovePacket constructor not found");
    }

    private Object newSpawnPacket(Object entity, UUID uuid, Location location) throws ReflectiveOperationException {
        Class<?> spawnClass = nmsClass(
                "net.minecraft.network.protocol.game.ClientboundAddPlayerPacket",
                "net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn"
        );

        for (Constructor<?> ctor : spawnClass.getConstructors()) {
            if (ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0].isInstance(entity)) {
                return ctor.newInstance(entity);
            }
        }

        int id = ((Number) entity.getClass().getMethod("getId").invoke(entity)).intValue();
        byte yaw = (byte) Math.floor(location.getYaw() * 256.0F / 360.0F);
        byte pitch = (byte) Math.floor(location.getPitch() * 256.0F / 360.0F);

        Object[] args = {id, uuid, location.getX(), location.getY(), location.getZ(), yaw, pitch};
        for (Constructor<?> ctor : spawnClass.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 7
                    && params[0] == int.class
                    && params[1] == UUID.class
                    && params[2] == double.class
                    && params[3] == double.class
                    && params[4] == double.class
                    && params[5] == byte.class
                    && params[6] == byte.class) {
                return ctor.newInstance(args);
            }
        }

        throw new NoSuchMethodException("ClientboundAddPlayerPacket constructor not found");
    }

    private Object newDestroyPacket(Object entity) throws ReflectiveOperationException {
        int id = ((Number) entity.getClass().getMethod("getId").invoke(entity)).intValue();

        Class<?> destroyClass = nmsClass(
                "net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket",
                "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy"
        );

        for (Constructor<?> ctor : destroyClass.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length != 1) {
                continue;
            }
            if (params[0] == int[].class) {
                return ctor.newInstance((Object) new int[]{id});
            }
            if (params[0].isArray() && params[0].getComponentType() == int.class) {
                Object array = Array.newInstance(int.class, 1);
                Array.setInt(array, 0, id);
                return ctor.newInstance(array);
            }
            if (params[0].isAssignableFrom(java.util.stream.IntStream.class)) {
                return ctor.newInstance(java.util.stream.IntStream.of(id));
            }
            if (Collection.class.isAssignableFrom(params[0])) {
                return ctor.newInstance(Collections.singletonList(id));
            }
            if (params[0] == Integer.class || params[0] == int.class) {
                return ctor.newInstance(id);
            }
        }

        throw new NoSuchMethodException("ClientboundRemoveEntitiesPacket constructor not found");
    }

    private void sendPacketToAll(Object packet) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                sendPacket(player, packet);
            } catch (ReflectiveOperationException ignored) {
            }
        }
    }

    private static void sendPacket(Player receiver, Object packet) throws ReflectiveOperationException {
        Object handle = receiver.getClass().getMethod("getHandle").invoke(receiver);
        Object connection = getConnection(handle);

        Method send = null;
        for (Method method : connection.getClass().getMethods()) {
            if (method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(packet.getClass())
                    && (method.getName().equals("send") || method.getName().equals("sendPacket"))) {
                send = method;
                break;
            }
        }
        if (send == null) {
            throw new NoSuchMethodException("send method not found");
        }

        send.invoke(connection, packet);
    }

    private static Object getConnection(Object handle) throws ReflectiveOperationException {
        for (String fieldName : new String[]{"connection", "playerConnection"}) {
            for (Class<?> type = handle.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object connection = field.get(handle);
                    if (connection != null) {
                        return connection;
                    }
                } catch (NoSuchFieldException ignored) {
                }
            }
        }
        throw new NoSuchFieldException("connection field not found");
    }

    private static Object getNmsServer() throws ReflectiveOperationException {
        return Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
    }

    private static Object getNmsWorld(World world) throws ReflectiveOperationException {
        return world.getClass().getMethod("getHandle").invoke(world);
    }

    private static void setEntityLocation(Object entity, Location location) throws ReflectiveOperationException {
        for (String methodName : new String[]{"moveTo", "setLocation", "setPositionRotation"}) {
            try {
                Method method = entity.getClass().getMethod(
                        methodName, double.class, double.class, double.class, float.class, float.class
                );
                method.invoke(entity, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
                return;
            } catch (NoSuchMethodException ignored) {
            }
        }

        boolean positionSet = false;
        for (String methodName : new String[]{"setPos", "setPosition"}) {
            try {
                Method method = entity.getClass().getMethod(methodName, double.class, double.class, double.class);
                method.invoke(entity, location.getX(), location.getY(), location.getZ());
                positionSet = true;
                break;
            } catch (NoSuchMethodException ignored) {
            }
        }
        if (!positionSet) {
            throw new NoSuchMethodException("Entity position setter not found");
        }

        for (String methodName : new String[]{"setYRot", "setYaw"}) {
            try {
                entity.getClass().getMethod(methodName, float.class).invoke(entity, location.getYaw());
                break;
            } catch (NoSuchMethodException ignored) {
            }
        }
        for (String methodName : new String[]{"setXRot", "setPitch"}) {
            try {
                entity.getClass().getMethod(methodName, float.class).invoke(entity, location.getPitch());
                break;
            } catch (NoSuchMethodException ignored) {
            }
        }
    }

    private static Class<?> nmsClass(String... names) throws ClassNotFoundException {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException("No NMS class found");
    }

    private static final class FakePlayer {

        private final String name;
        private final UUID uuid;
        private final Object entity;
        private final World world;
        private final int chunkX;
        private final int chunkZ;
        private final Plugin plugin;

        private FakePlayer(String name, UUID uuid, Object entity, World world, int chunkX, int chunkZ, Plugin plugin) {
            this.name = name;
            this.uuid = uuid;
            this.entity = entity;
            this.world = world;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.plugin = plugin;
        }
    }
}