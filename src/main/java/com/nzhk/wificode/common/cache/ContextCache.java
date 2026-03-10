package com.nzhk.wificode.common.cache;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class ContextCache {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static String getUserId() {
        return getUser() != null ? getUser().getId() : null;
    }

    public static UserInfo getUser() {
        return THREAD_LOCAL.get();
    }

    public static void setUserInfo(UserInfo userInfo) {
        THREAD_LOCAL.set(userInfo);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
