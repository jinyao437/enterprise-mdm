package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * IM 业务通知发送状态（本地消息表状态机）
 */
@Getter
@AllArgsConstructor
public enum ImNotifyStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待发送"),
    SENT(1, "已送达"),
    FAILED(2, "发送失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ImNotifyStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(ImNotifyStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
