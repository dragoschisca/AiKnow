package com.aiknow.security;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> userId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> organizationId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> workspaceId = new ThreadLocal<>();

    public static void setUserId(UUID id) {
        userId.set(id);
    }

    public static UUID getUserId() {
        return userId.get();
    }

    public static void setOrganizationId(UUID id) {
        organizationId.set(id);
    }

    public static UUID getOrganizationId() {
        return organizationId.get();
    }

    public static void setWorkspaceId(UUID id) {
        workspaceId.set(id);
    }

    public static UUID getWorkspaceId() {
        return workspaceId.get();
    }

    public static void clear() {
        userId.remove();
        organizationId.remove();
        workspaceId.remove();
    }
}
