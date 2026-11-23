package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Webhook 可订阅事件（三方应用通过开放平台订阅）
 */
@Getter
@AllArgsConstructor
public enum WebhookEventEnum implements ArrayValuable<String> {

    DEVICE_REGISTERED("device.registered", "设备注册"),
    DEVICE_ALERT("device.alert", "设备告警"),
    COMPLIANCE_VIOLATION("compliance.violation", "合规违规"),
    APPROVAL_RESULT("approval.result", "审批结果"),
    APP_DISTRIBUTION("app.distribution", "应用分发");

    public static final String[] ARRAYS = Arrays.stream(values()).map(WebhookEventEnum::getEvent).toArray(String[]::new);

    private final String event;
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

    public static String nameOf(String event) {
        return Arrays.stream(values())
                .filter(e -> e.event.equals(event))
                .map(WebhookEventEnum::getName)
                .findFirst().orElse("未知");
    }

    public static boolean isValid(String event) {
        return Arrays.stream(values()).anyMatch(e -> e.event.equals(event));
    }
}
