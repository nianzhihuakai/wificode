package com.nzhk.wificode.common.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 同一 key 每个自然日最多 maxPerDay 次成功占用（用于绑定尝试等）。
 * 内存实现；多实例部署请改用 Redis。
 */
public class DailyAttemptLimiter {

    private final int maxPerDay;
    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private volatile String lastSeenDate = "";

    public DailyAttemptLimiter(int maxPerDay) {
        this.maxPerDay = maxPerDay;
    }

    private ZoneId zone() {
        return ZoneId.systemDefault();
    }

    /**
     * @param scope 例如 userId
     * @return true 表示允许本次尝试（已计入次数）
     */
    public boolean tryAcquire(String scope) {
        LocalDate today = LocalDate.now(zone());
        String day = today.toString();
        String key = scope + ":" + day;
        cleanupIfNewDay(day);
        AtomicInteger n = counts.computeIfAbsent(key, k -> new AtomicInteger(0));
        int c = n.incrementAndGet();
        if (c > maxPerDay) {
            n.decrementAndGet();
            return false;
        }
        return true;
    }

    private synchronized void cleanupIfNewDay(String today) {
        if (today.equals(lastSeenDate)) {
            return;
        }
        lastSeenDate = today;
        counts.keySet().removeIf(k -> !k.endsWith(":" + today));
    }
}
