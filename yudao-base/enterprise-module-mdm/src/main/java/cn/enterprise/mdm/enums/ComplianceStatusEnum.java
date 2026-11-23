package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 设备合规检查状态（MDM 独有）
 */
@Getter
@AllArgsConstructor
public enum ComplianceStatusEnum implements ArrayValuable<Integer> {

    COMPLIANT(0, "合规"),
    NON_COMPLIANT(1, "不合规"),
    PENDING(2, "待复核");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ComplianceStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(ComplianceStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
