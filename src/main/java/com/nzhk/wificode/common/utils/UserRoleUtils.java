package com.nzhk.wificode.common.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class UserRoleUtils {

    public static final String ROLE_SALES = "SALES";
    public static final String ROLE_STORE = "STORE";
    public static final String ROLE_ADMIN = "ADMIN";

    private UserRoleUtils() {
    }

    public static List<String> toList(String[] roles) {
        if (roles == null || roles.length == 0) {
            return Collections.singletonList(ROLE_SALES);
        }
        return Arrays.stream(roles)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public static boolean isAdmin(String[] roles) {
        if (roles == null) {
            return false;
        }
        for (String r : roles) {
            if (r != null && ROLE_ADMIN.equalsIgnoreCase(r.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String[] defaultRoles() {
        return new String[]{ROLE_SALES};
    }
}
