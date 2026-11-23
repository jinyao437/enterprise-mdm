package com.enterprise.mdm.agent.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.enterprise.mdm.agent.owner.DeviceOwnerManager;

/**
 * 本地指令桥：接收 uni-app 工作台或系统发出的管控指令。
 *
 * <p>这是 agent 与上层 App 的解耦接口——uni-app 通过原生插件发广播到此，
 * agent 执行原生能力。第三方 App 无 BIND_AGENT 权限无法调用（Manifest 已声明权限保护）。
 */
public class AgentCommandReceiver extends BroadcastReceiver {

    public static final String ACTION_COMMAND = "com.enterprise.mdm.agent.action.COMMAND";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_CONFIRM_TOKEN = "confirmToken";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_COMMAND.equals(intent.getAction())) {
            return;
        }
        String command = intent.getStringExtra(EXTRA_COMMAND);
        if (command == null) {
            return;
        }
        switch (command) {
            case "LOCK":
                DeviceOwnerManager.lockNow(context);
                break;
            case "WIPE":
                // 高危：必须有二次确认令牌（与后端 WIPE 确认机制一致）
                String token = intent.getStringExtra(EXTRA_CONFIRM_TOKEN);
                if (token == null || token.isEmpty()) {
                    return; // 缺令牌拒绝擦除，fail-closed
                }
                DeviceOwnerManager.wipeData(context, 0);
                break;
            default:
                break;
        }
    }
}
