package com.tahai.hookwhitelist.hook;

import com.tahai.hookwhitelist.whitelist.WhitelistManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerHookListener implements Listener {

    private final WhitelistManager whitelistManager;
    private final HookPullTask hookPullTask;

    /** 钩子 UUID → 被勾玩家 UUID */
    private final Map<UUID, UUID> hookToTarget = new HashMap<>();
    /** 被勾玩家 UUID → 当前生效的钩子 UUID（用于区分多钩场景，避免旧钩误清理新会话） */
    private final Map<UUID, UUID> targetToHook = new HashMap<>();
    /** 钩子 UUID → 钩子实体引用（用于判断钩子是否已失效） */
    private final Map<UUID, FishHook> hooks = new HashMap<>();

    public PlayerHookListener(Plugin plugin, WhitelistManager whitelistManager, HookPullTask hookPullTask) {
        this.whitelistManager = whitelistManager;
        this.hookPullTask = hookPullTask;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        FishHook hook = event.getHook();
        if (hook == null) {
            return;
        }
        Player fisher = event.getPlayer();

        if (event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            Entity caught = event.getCaught();
            if (!(caught instanceof Player target)) {
                return;
            }
            if (target.getUniqueId().equals(fisher.getUniqueId())) {
                return;
            }
            // 非白名单玩家勾中玩家时不介入，保持原版行为
            if (!whitelistManager.contains(fisher.getUniqueId())) {
                return;
            }
            if (!hook.isValid()) {
                return;
            }
            UUID targetId = target.getUniqueId();
            UUID previousTarget = hookToTarget.get(hook.getUniqueId());
            if (previousTarget != null && !previousTarget.equals(targetId)) {
                releaseHook(hook.getUniqueId());
            }
            UUID previousHook = targetToHook.get(targetId);
            if (previousHook != null && !previousHook.equals(hook.getUniqueId())) {
                // 同一玩家被新钩子勾住：旧钩子的映射立即作废，防止旧钩子之后的 REEL_IN 误结束新会话
                hookToTarget.remove(previousHook);
                hooks.remove(previousHook);
            }
            hookToTarget.put(hook.getUniqueId(), targetId);
            targetToHook.put(targetId, hook.getUniqueId());
            hooks.put(hook.getUniqueId(), hook);
            hookPullTask.startSession(fisher, hook, target);
        } else if (event.getState() == PlayerFishEvent.State.REEL_IN
                || event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {
            releaseHook(hook.getUniqueId());
        }

        pruneInvalidHooks();
    }

    /**
     * 结束指定钩子拥有的会话；仅当该钩子仍是该玩家当前生效的钩子时才真正调用 endSession，
     * 避免多钩同目标时旧钩子误清理新会话。
     */
    private void releaseHook(UUID hookId) {
        UUID targetId = hookToTarget.remove(hookId);
        hooks.remove(hookId);
        if (targetId == null) {
            return;
        }
        if (hookId.equals(targetToHook.get(targetId))) {
            targetToHook.remove(targetId);
            hookPullTask.endSession(targetId);
        }
    }

    /** 钩子已失效（脱钩、实体被移除）时清理其残留映射并结束对应会话，避免修饰器残留。 */
    private void pruneInvalidHooks() {
        if (hookToTarget.isEmpty()) {
            return;
        }
        for (UUID hookId : new ArrayList<>(hookToTarget.keySet())) {
            FishHook hook = hooks.get(hookId);
            if (hook == null || !hook.isValid()) {
                releaseHook(hookId);
            }
        }
    }
}