package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 远程指令状态机：CREATED -> DISPATCHED -> ACKED / FAILED（失败可重试）
 */
@Getter
@AllArgsConstructor
public enum CommandStatusEnum implements ArrayValuable<Integer> {

    CREATED(0, "已创建"),
    DISPATCHED(1, "已下发"),
    ACKED(2, "设备已确认"),
    FAILED(3, "下发失败");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CommandStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(CommandStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
