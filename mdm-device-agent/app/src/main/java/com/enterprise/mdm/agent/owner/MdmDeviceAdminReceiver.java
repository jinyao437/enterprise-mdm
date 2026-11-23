package com.enterprise.mdm.agent.owner;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Device Owner 激活回调。
 *
 * <p>只有成为 Device Owner 后，agent 才能调用远程擦除/锁屏/静默安装等系统级能力。
 * 激活方式：出厂预置 MDM、QR Code provisioning 或 adb shell dpm set-device-owner。
 */
public class MdmDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "设备管理员已激活", Toast.LENGTH_SHORT).show();
        DeviceOwnerManager.onProvisioned(context);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "取消设备管理员将失去企业设备管控，请联系管理员。";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        DeviceOwnerManager.onUnprovisioned(context);
    }
}
