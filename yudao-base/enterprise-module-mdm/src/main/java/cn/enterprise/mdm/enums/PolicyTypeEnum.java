package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备策略类型（MDM 独有）
 */
@Getter
@AllArgsConstructor
public enum PolicyTypeEnum implements ArrayValuable<Integer> {

    SECURITY(1, "安全策略"),
    NETWORK(2, "网络策略"),
    APPLICATION(3, "应用策略"),
    CONTENT(4, "内容策略");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PolicyTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer type) {
        return Arrays.stream(values())
                .filter(e -> e.type.equals(type))
                .map(PolicyTypeEnum::getName)
                .findFirst().orElse("未知");
    }
}
