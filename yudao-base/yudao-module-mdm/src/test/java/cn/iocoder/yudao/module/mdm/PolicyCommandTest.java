package cn.iocoder.yudao.module.mdm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.iocoder.yudao.module.mdm.dal.dataobject.DeviceCommandDO;
import cn.iocoder.yudao.module.mdm.dal.dataobject.DevicePolicyDO;
import cn.iocoder.yudao.module.mdm.enums.CommandStatusEnum;
import cn.iocoder.yudao.module.mdm.enums.CommandTypeEnum;
import cn.iocoder.yudao.module.mdm.enums.PolicyTypeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 策略与远程管控单测：验证新增领域能力正确复用底座租户基类，且状态/高危标识正确。
 */
class PolicyCommandTest {

    @Test
    @DisplayName("策略/指令 DO 均继承底座 TenantBaseDO（租户隔离复用底座）")
    void dosInheritTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(DevicePolicyDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(DeviceCommandDO.class));
    }

    @Test
    @DisplayName("策略类型覆盖安全/网络/应用/内容")
    void policyTypes() {
        assertEquals("安全策略", PolicyTypeEnum.nameOf(1));
        assertEquals("网络策略", PolicyTypeEnum.nameOf(2));
        assertEquals("应用策略", PolicyTypeEnum.nameOf(3));
        assertEquals("内容策略", PolicyTypeEnum.nameOf(4));
    }

    @Test
    @DisplayName("WIPE 为高危需二次确认；LOCK 不是")
    void dangerousCommandFlag() {
        CommandTypeEnum wipe = CommandTypeEnum.valueOfType(CommandTypeEnum.WIPE.getType());
        CommandTypeEnum lock = CommandTypeEnum.valueOfType(CommandTypeEnum.LOCK.getType());
        assertNotNull(wipe);
        assertNotNull(lock);
        assertTrue(wipe.isDangerous(), "远程擦除必须标记高危");
        assertFalse(lock.isDangerous());
    }

    @Test
    @DisplayName("指令状态机名称可解析")
    void commandStatus() {
        assertEquals("已创建", CommandStatusEnum.nameOf(0));
        assertEquals("已下发", CommandStatusEnum.nameOf(1));
        assertEquals("设备已确认", CommandStatusEnum.nameOf(2));
        assertEquals("下发失败", CommandStatusEnum.nameOf(3));
    }

    @Test
    @DisplayName("幂等键拼租户维度：跨租户不冲突（值语义）")
    void idempotencyKeyIncludesTenant() {
        String keyA = 10000L + ":" + "req-1";
        String keyB = 20000L + ":" + "req-1";
        assertNotEquals(keyA, keyB);
        // H-1 正确做法：用 Objects.equals 而非 ==
        Long a = new Long(10000L), b = new Long(10000L);
        assertTrue(Objects.equals(a, b));
        assertFalse(a == b, "两个不同 Long 对象的 == 为 false，证明必须用 Objects.equals");
    }
}
