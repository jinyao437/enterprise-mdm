package com.enterprise.mdm.agent.heartbeat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.enterprise.mdm.agent.api.AgentConfig;
import com.enterprise.mdm.agent.api.OkHttpAgentApi;
import com.enterprise.mdm.agent.owner.DeviceOwnerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备心跳上报：周期采集设备状态并上报后端（真实 HTTP）。
 *
 * <p>未配置服务端时 fail-closed（返回 failure，不假装成功），
 * 网络失败由 WorkManager 退避重试。
 */
public class HeartbeatWorker extends Worker {

    public HeartbeatWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        // 未配置服务端：不重试（重试也没用），等待用户配置
        if (!AgentConfig.isConfigured(ctx)) {
            return Result.failure();
        }
        try {
            Map<String, Object> status = collectStatus(ctx);
            new OkHttpAgentApi(ctx).reportHeartbeat(status);
            return Result.success();
        } catch (Exception e) {
            // 网络/业务失败：返回 retry，由 WorkManager 退避重试（必达语义）
            return Result.retry();
        }
    }

    /** 采集设备状态（真实字段：Device Owner 状态、平台、版本、设备编号） */
    private Map<String, Object> collectStatus(Context ctx) {
        Map<String, Object> m = new HashMap<>();
        m.put("deviceId", AgentConfig.deviceId(ctx));
        m.put("platform", "android");
        m.put("osVersion", android.os.Build.VERSION.RELEASE);
        m.put("deviceOwner", DeviceOwnerManager.isDeviceOwner(ctx));
        return m;
    }
}
