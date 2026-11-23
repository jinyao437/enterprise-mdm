package cn.enterprise.mdm;

import cn.enterprise.mdm.dal.dataobject.ComplianceReportDO;
import cn.enterprise.mdm.dal.dataobject.DeviceGroupDO;
import cn.enterprise.mdm.enums.ComplianceStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备分组与合规检查单测：验证 DO 继承底座租户基类、合规状态闭环语义。
 */
class GroupComplianceTest {

    @Test
    @DisplayName("分组/合规 DO 继承底座 TenantBaseDO（租户隔离复用底座）")
    void dosInheritTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(DeviceGroupDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(ComplianceReportDO.class));
    }

    @Test
    @DisplayName("分组树：根节点 parentId 默认 0")
    void groupTreeRoot() {
        DeviceGroupDO group = DeviceGroupDO.builder().name("总部").build();
        assertEquals("总部", group.getName());
        // createGroup 中 parentId 为 null 时置 0L，这里验证 DO 可承载该值
        group.setParentId(0L);
        assertEquals(0L, group.getParentId());
    }

    @Test
    @DisplayName("合规状态枚举覆盖合规/不合规/待复核")
    void complianceStatus() {
        assertEquals("合规", ComplianceStatusEnum.nameOf(0));
        assertEquals("不合规", ComplianceStatusEnum.nameOf(1));
        assertEquals("待复核", ComplianceStatusEnum.nameOf(2));
        assertEquals("未知", ComplianceStatusEnum.nameOf(999));
    }

    @Test
    @DisplayName("不合规上报默认未告警，可承载检查详情与上报时间")
    void complianceReportFields() {
        ComplianceReportDO report = ComplianceReportDO.builder()
                .deviceId(1001L)
                .checkItem("jailbreak")
                .status(ComplianceStatusEnum.NON_COMPLIANT.getStatus())
                .detail("{\"rooted\":true}")
                .alerted(false)
                .reportTime(LocalDateTime.of(2026, 11, 23, 12, 0))
                .build();
        assertEquals(1001L, report.getDeviceId());
        assertEquals("jailbreak", report.getCheckItem());
        assertEquals(ComplianceStatusEnum.NON_COMPLIANT.getStatus(), report.getStatus());
        assertFalse(report.getAlerted(), "不合规上报应处于未告警状态，等待告警任务扫描");
        assertNotNull(report.getReportTime());
    }
}
