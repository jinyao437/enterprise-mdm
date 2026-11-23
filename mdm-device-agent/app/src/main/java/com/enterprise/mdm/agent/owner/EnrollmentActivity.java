package com.enterprise.mdm.agent.owner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 设备激活入口页。
 *
 * <p>展示当前 Device Owner 状态与激活引导。真正激活靠：
 * 1) 出厂预置 MDM（OEM 合作）
 * 2) QR Code provisioning（Android 7+，扫码后自动设为 Device Owner）
 * 3) adb shell dpm set-device-owner（开发/测试）
 * 普通应用市场安装无法获得 Device Owner 权限——这是 MDM 的产品前提。
 */
public class EnrollmentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        boolean owner = DeviceOwnerManager.isDeviceOwner(this);
        view.setText(owner ? "设备已纳管（Device Owner 已激活）" : "设备未纳管：需通过 QR 码或出厂预置激活为 Device Owner");
        view.setTextSize(16);
        view.setPadding(40, 80, 40, 40);
        setContentView(view);
    }
}
