package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备状态枚举（MDM 独有，芋道底座无此概念）
 */
@Getter
@AllArgsConstructor
public enum DeviceStatusEnum implements ArrayValuable<Integer> {

    REGISTERED(0, "已注册"),
    ACTIVE(1, "使用中"),
    LOST(2, "已挂失"),
    DISABLED(3, "已停用"),
    RETIRED(4, "已报废");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(DeviceStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(DeviceStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
