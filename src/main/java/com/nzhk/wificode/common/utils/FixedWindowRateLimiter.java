package com.nzhk.wificode.common.utils;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 滑动窗口限流：每 key 每 windowMs 毫秒最多 maxPerWindow 次
 */
public class FixedWindowRateLimiter {

    private final long windowMs;
    private final int maxPerWindow;
    private final ConcurrentHashMap<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(long windowMs, int maxPerWindow) {
        this.windowMs = windowMs;
        this.maxPerWindow = maxPerWindow;
    }

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long start = now - windowMs;
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
