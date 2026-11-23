package com.enterprise.mdm.agent.api;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Agent 连接配置（服务端地址/租户/token/设备编号）。
 *
 * <p>从 SharedPreferences 读取，由激活流程注入；绝不硬编码密钥。
 * 注意：生产环境 token 应加密存储（Keystore + EncryptedSharedPreferences），
 * 这里用普通 SP 仅作骨架，加密为待办项。
 */
public final class AgentConfig {

    private static final String PREF = "mdm_agent_config";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_TENANT_ID = "tenant_id";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_DEVICE_ID = "device_id";

    private AgentConfig() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static void save(Context c, String serverUrl, String tenantId, String token, Long deviceId) {
        prefs(c).edit()
                .putString(KEY_SERVER_URL, serverUrl)
                .putString(KEY_TENANT_ID, tenantId)
                .putString(KEY_TOKEN, token)
                .putLong(KEY_DEVICE_ID, deviceId == null ? 0L : deviceId)
                .apply();
    }

    public static String serverUrl(Context c) { return prefs(c).getString(KEY_SERVER_URL, ""); }
    public static String tenantId(Context c) { return prefs(c).getString(KEY_TENANT_ID, ""); }
    public static String token(Context c) { return prefs(c).getString(KEY_TOKEN, ""); }
    public static Long deviceId(Context c) { return prefs(c).getLong(KEY_DEVICE_ID, 0L); }

    /** 是否已配置服务端 */
    public static boolean isConfigured(Context c) {
        String url = serverUrl(c);
        return url != null && !url.trim().isEmpty();
    }
}
