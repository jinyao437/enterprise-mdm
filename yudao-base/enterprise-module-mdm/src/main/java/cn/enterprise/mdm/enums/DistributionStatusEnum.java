package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 应用分发任务状态（含灰度）
 */
@Getter
@AllArgsConstructor
public enum DistributionStatusEnum implements ArrayValuable<Integer> {

    PENDING(0, "待下发"),
    GRAY(1, "灰度中"),
    FULL(2, "全量完成"),
    PAUSED(3, "已暂停"),
    CANCELLED(4, "已取消");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(DistributionStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(DistributionStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
