package com.enterprise.mdm.agent.api;

import java.util.Map;

/**
 * Agent 与后端通信接口定义（对接 enterprise-module-mdm）。
 *
 * <p>真实实现用 OkHttp 调用后端 API；设备标识、租户等由后端在下发策略时携带，
 * agent 不硬编码密钥。这里定义契约，具体网络调用留 TODO。
 */
public interface AgentApi {

    /** 设备激活上报（对应后端设备注册） */
    void reportEnrollment(String identifier, String platform, String osVersion);

    /** 心跳上报（设备状态 + 合规） */
    void reportHeartbeat(Map<String, Object> deviceStatus);

    /** 拉取设备策略（对应后端 /mdm/policy/effective?scopeType=DEVICE&scopeId=xxx） */
    String fetchPolicies(Long deviceId);

    /** 上报指令执行结果（对应后端 /mdm/command 状态更新） */
    void reportCommandResult(Long commandId, boolean success, String message);
}
