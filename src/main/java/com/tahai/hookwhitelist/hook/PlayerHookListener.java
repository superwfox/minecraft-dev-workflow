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
    /** 钩子 UUID → 钩子实体引用（仅用于清理过期的映射，不据此结束会话） */
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

        // 先清理历史映射残留：CAUGHT_ENTITY 之后原版会立刻 discard 浮标，
        // 因此这里只做映射清理，绝不因钩子失效而 endSession（会话由 HookPullTask 的固定窗口自行结束）。
        // 放在状态处理之前，避免刚登记的本次钩子被误清理。
        pruneStaleMappings();

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
                forgetHook(hook.getUniqueId());
                endSessionIfActive(hook.getUniqueId(), previousTarget);
            }
            UUID previousHook = targetToHook.get(targetId);
            if (previousHook != null && !previousHook.equals(hook.getUniqueId())) {
                // 同一玩家被新钩子勾住：旧钩子的映射立即作废，防止旧钩子之后的 REEL_IN 误结束新会话
                forgetHook(previousHook);
            }
            hookToTarget.put(hook.getUniqueId(), targetId);
            targetToHook.put(targetId, hook.getUniqueId());
            hooks.put(hook.getUniqueId(), hook);
            hookPullTask.startSession(fisher, hook, target);
        } else if (event.getState() == PlayerFishEvent.State.REEL_IN
                || event.getState() == PlayerFishEvent.State.FAILED_ATTEMPT) {
            // REEL_IN / FAILED_ATTEMPT 只会出现在“未勾中实体”的抛竿上；
            // 若确实存在映射会话，则按需求 5 结束会话并移除减速修饰器。
            releaseHook(hook.getUniqueId());
        }
    }

    /**
     * REEL_IN / FAILED_ATTEMPT 时结束该钩子拥有的会话；仅当该钩子仍是该玩家当前生效的钩子时才真正调用 endSession，
     * 避免多钩同目标时旧钩子误清理新会话。endSession 由 HookPullTask 内部按 slowKey 精确移除修饰器，重复调用安全。
     */
    private void releaseHook(UUID hookId) {
        UUID targetId = hookToTarget.remove(hookId);
        hooks.remove(hookId);
        if (targetId == null) {
            return;
        }
        endSessionIfActive(hookId, targetId);
    }

    /** 仅当 hookId 仍是该目标玩家当前生效的钩子时才结束会话。 */
    private void endSessionIfActive(UUID hookId, UUID targetId) {
        if (hookId.equals(targetToHook.get(targetId))) {
            targetToHook.remove(targetId);
            hookPullTask.endSession(targetId);
        }
    }

    /** 丢弃指定钩子的全部映射，不触碰会话状态（用于旧钩子被新钩子取代的场景）。 */
    private void forgetHook(UUID hookId) {
        hookToTarget.remove(hookId);
        hooks.remove(hookId);
        targetToHook.values().removeIf(hookId::equals);
    }

    /**
     * 仅清理已失效钩子遗留的映射表项，避免映射无限增长。
     * 注意：CAUGHT_ENTITY 后原版会 discard 浮标，钩子失效是正常现象，
     * 因此这里绝对不能调用 endSession，否则会在拉拽窗口内提前中断拉拽并移除减速修饰器。
     * 会话的结束交给 HookPullTask（固定 tick 窗口、离线、死亡、跨世界、插件禁用）负责。
     */
    private void pruneStaleMappings() {
        if (hookToTarget.isEmpty()) {
            return;
        }
        for (UUID hookId : new ArrayList<>(hookToTarget.keySet())) {
            FishHook hook = hooks.get(hookId);
            if (hook == null || !hook.isValid()) {
                forgetHook(hookId);
            }
        }
    }
}