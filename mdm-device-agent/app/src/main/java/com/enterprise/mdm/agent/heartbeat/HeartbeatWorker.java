package com.enterprise.mdm.agent.heartbeat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * 设备心跳上报：周期性把设备状态、合规信息上报给后端。
 *
 * <p>真实接入时在此调用 AgentApi 上报；后端接口见 enterprise-module-mdm 的
 * /mdm/device/status 与 /mdm/compliance/report。
 */
public class HeartbeatWorker extends Worker {

    public HeartbeatWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // TODO 1. 采集设备状态（Device Owner 状态、电量、网络、已装应用等）
        // TODO 2. 调用 AgentApi.reportHeartbeat(...) 上报到后端
        // TODO 3. 失败返回 Result.retry() 由 WorkManager 退避重试
        return Result.success();
    }
}
