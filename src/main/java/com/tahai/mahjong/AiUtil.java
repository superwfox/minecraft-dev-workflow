package com.tahai.mahjong;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI 决策静态工具类
 */
public final class AiUtil {

    private static final Random RANDOM = new Random();

    private AiUtil() {}

    /**
     * AI 档位
     */
    public enum AiLevel {
        BEGINNER,
        JUNIOR,
        INTERMEDIATE,
        ADVANCED,
        MASTER
    }

    /**
     * 决策结果
     */
    public record AiDecision(
            String action,  // play / peng / gang / hu / draw
            String tile     // 牌面，如 "1m", "5s"
    ) {}

    /**
     * 根据玩家手牌、副露、牌河、当前最后一张牌（摸牌或他人打出的牌）计算决策
     *
     * @param hand     当前手牌列表
     * @param melds    已副露组合（碰/杠/吃）
     * @param river    牌河（已打出的牌）
     * @param lastTile 当前回合最后一张牌（玩家自己摸到或他人打出的牌），可为 null
     * @param level    机器人档位
     * @return 决策结果
     */
    public static AiDecision decide(
            List<String> hand,
            List<String> melds,
            List<String> river,
            String lastTile,
            AiLevel level
    ) {
        // 最低复杂度：直接出牌
        return playAnyTile(hand);
    }

    private static AiDecision playAnyTile(List<String> hand) {
        if (hand.isEmpty()) {
            return new AiDecision("play", "");
        }
        String tile = hand.get(RANDOM.nextInt(hand.size()));
        return new AiDecision("play", tile);
    }
}