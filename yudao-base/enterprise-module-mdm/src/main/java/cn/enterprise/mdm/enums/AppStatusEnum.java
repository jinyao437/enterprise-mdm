package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 企业应用状态（MDM 应用分发）
 */
@Getter
@AllArgsConstructor
public enum AppStatusEnum implements ArrayValuable<Integer> {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已上架"),
    OFFLINE(2, "已下架");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AppStatusEnum::getStatus).toArray(Integer[]::new);

    private final Integer status;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static String nameOf(Integer status) {
        return Arrays.stream(values())
                .filter(e -> e.status.equals(status))
                .map(AppStatusEnum::getName)
                .findFirst().orElse("未知");
    }
}
