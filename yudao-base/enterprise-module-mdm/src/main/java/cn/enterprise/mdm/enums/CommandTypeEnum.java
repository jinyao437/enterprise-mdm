package cn.enterprise.mdm.enums;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 远程管控指令类型（MDM 独有）
 */
@Getter
@AllArgsConstructor
public enum CommandTypeEnum implements ArrayValuable<Integer> {

    LOCK(1, "远程锁定", false),
    WIPE(2, "远程擦除", true),      // 高危：需要二次确认
    LOCATE(3, "定位", false),
    PUSH_APP(4, "应用推送", false),
    POLICY_REFRESH(5, "策略刷新", false);

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CommandTypeEnum::getType).toArray(Integer[]::new);

    private final Integer type;
    private final String name;
    /** 是否高危（需二次确认令牌） */
    private final boolean dangerous;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static CommandTypeEnum valueOfType(Integer type) {
        return Arrays.stream(values()).filter(e -> e.type.equals(type)).findFirst().orElse(null);
    }

    public static String nameOf(Integer type) {
        CommandTypeEnum e = valueOfType(type);
        return e == null ? "未知" : e.getName();
    }
}
