package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IM 业务通知消息类型（业务联动用，区别于底座 IM 的聊天消息）
 */
@Getter
@AllArgsConstructor
public enum ImMessageTypeEnum implements ArrayValuable<Integer> {

    APPROVAL_RESULT(1, "审批结果通知"),
    DEVICE_ALERT(2, "设备告警"),
    POLICY_CHANGED(3, "策略变更"),
    COMPLIANCE_ALERT(4, "合规告警"),
    APP_DISTRIBUTION(5, "应用分发通知");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImMessageTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer type) {
        return Arrays.stream(values())
                .filter(e -> e.type.equals(type))
                .map(ImMessageTypeEnum::getName)
                .findFirst().orElse("未知");
    }
}
