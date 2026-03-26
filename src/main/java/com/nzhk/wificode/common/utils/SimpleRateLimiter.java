package com.nzhk.wificode.common.utils;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 简单滑动窗口限流：每 key 每分钟最多 maxPerWindow 次
 */
public class SimpleRateLimiter {

    private static final long WINDOW_MS = 60_000L;

    private final int maxPerWindow;
    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public SimpleRateLimiter(int maxPerWindow) {
        this.maxPerWindow = maxPerWindow;
    }

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long start = now - WINDOW_MS;
        Deque<Long> q = buckets.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && q.peekFirst() < start) {
                q.pollFirst();
            }
            if (q.size() >= maxPerWindow) {
                return false;
            }
            q.addLast(now);
            return true;
        }
    }
}
