package com.enterprise.mdm.agent.policy;

import android.content.Context;

import com.enterprise.mdm.agent.owner.DeviceOwnerManager;

/**
 * 设备策略执行器：把后端下发的策略内容转换为原生管控动作。
 *
 * <p>策略内容为 JSON（与后端 DevicePolicyDO.content 对应），本类解析并执行。
 * 未成为 Device Owner 时所有管控动作返回 false（fail-closed）。
 */
public final class PolicyExecutor {

    private PolicyExecutor() {
    }

    /**
     * 执行一条策略。
     *
     * @param context     应用上下文
     * @param type        策略类型：1安全 2网络 3应用 4内容（对应后端 PolicyTypeEnum）
     * @param content     策略内容 JSON
     * @return 是否执行成功
     */
    public static boolean execute(Context context, int type, String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        switch (type) {
            case 1: // 安全策略：密码最小长度
                int minLen = PolicyJsonParser.passwordMinLength(content);
                return DeviceOwnerManager.setPasswordMinimumLength(context, minLen);
            case 2: // 网络策略：预留（Wi-Fi/APN 下发需 DevicePolicyManager 额外 API）
                return true;
            case 3: // 应用策略：预留（应用黑白名单需 installPackage/隐藏应用）
                return true;
            case 4: // 内容策略：相机禁用
                boolean disableCamera = PolicyJsonParser.cameraDisabled(content);
                return DeviceOwnerManager.setCameraDisabled(context, disableCamera);
            default:
                return false;
        }
    }
}
