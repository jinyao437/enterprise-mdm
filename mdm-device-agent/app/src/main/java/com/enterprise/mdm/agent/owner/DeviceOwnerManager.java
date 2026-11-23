package com.enterprise.mdm.agent.owner;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

/**
 * Device Owner 能力封装（原生系统级 API，uni-app 无法直接调用）。
 *
 * <p>所有高危操作都先校验 isDeviceOwner，未激活时拒绝——fail-closed。
 */
public final class DeviceOwnerManager {

    private DeviceOwnerManager() {
    }

    private static ComponentName adminComponent(Context context) {
        return new ComponentName(context, MdmDeviceAdminReceiver.class);
    }

    /** 当前 app 是否已是 Device Owner */
    public static boolean isDeviceOwner(Context context) {
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm != null && dpm.isDeviceOwnerApp(context.getPackageName());
    }

    /** 激活成功后的初始化：拉策略、注册心跳 */
    public static void onProvisioned(Context context) {
        // TODO 通知后端设备已激活，触发首次策略下发
        // 由 HeartbeatWorker / PolicySyncWorker 接管
    }

    /** 激活被取消 */
    public static void onUnprovisioned(Context context) {
        // TODO 上报异常，停止管控
    }

    /** 远程锁定 */
    public static boolean lockNow(Context context) {
        if (!isDeviceOwner(context)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            dpm.lockNow();
            return true;
        }
        return false;
    }

    /** 远程擦除（高危）：Device Owner 才能调用，且会清空设备所有数据 */
    public static boolean wipeData(Context context, int flags) {
        if (!isDeviceOwner(context)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            dpm.wipeData(flags);
            return true;
        }
        return false;
    }

    /** 禁用相机 */
    public static boolean setCameraDisabled(Context context, boolean disabled) {
        if (!isDeviceOwner(context)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            dpm.setCameraDisabled(adminComponent(context), disabled);
            return true;
        }
        return false;
    }

    /** 设置密码最小长度（安全策略） */
    public static boolean setPasswordMinimumLength(Context context, int length) {
        if (!isDeviceOwner(context)) {
            return false;
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null) {
            dpm.setPasswordMinimumLength(adminComponent(context), length);
            return true;
        }
        return false;
    }
}
