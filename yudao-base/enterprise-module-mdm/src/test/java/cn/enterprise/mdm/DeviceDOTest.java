package cn.enterprise.mdm;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import cn.enterprise.mdm.dal.dataobject.DeviceDO;
import cn.enterprise.mdm.enums.DeviceStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 MDM 设备模块正确复用芋道底座能力（租户隔离、状态枚举），而非重复实现。
 */
class DeviceDOTest {

    @Test
    @DisplayName("设备 DO 继承 TenantBaseDO：租户隔离由底座插件提供")
    void deviceInheritsTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(DeviceDO.class),
                "DeviceDO 必须继承底座 TenantBaseDO，复用 MyBatis-Plus 租户插件");
    }

    @Test
    @DisplayName("设备 DO 可设置 tenantId（来自基类）与设备字段")
    void deviceFieldsWork() {
        DeviceDO device = DeviceDO.builder()
                .identifier("IMEI-123")
                .name("测试设备")
                .platform("android")
                .status(DeviceStatusEnum.ACTIVE.getStatus())
                .build();
        device.setTenantId(10000L);
        assertEquals(10000L, device.getTenantId());
        assertEquals("IMEI-123", device.getIdentifier());
        assertEquals(DeviceStatusEnum.ACTIVE.getStatus(), device.getStatus());
    }

    @Test
    @DisplayName("设备状态枚举覆盖 MDM 生命周期且名称可解析")
    void statusEnumCoversLifecycle() {
        assertEquals("使用中", DeviceStatusEnum.nameOf(1));
        assertEquals("已挂失", DeviceStatusEnum.nameOf(2));
        assertEquals("未知", DeviceStatusEnum.nameOf(999));
    }
}
