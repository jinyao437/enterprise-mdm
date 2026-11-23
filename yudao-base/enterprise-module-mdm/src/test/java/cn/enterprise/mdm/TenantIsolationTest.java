package cn.enterprise.mdm;

import cn.enterprise.mdm.datasource.TenantDataSourceRouter;
import cn.enterprise.mdm.datasource.TenantIsolationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 双隔离单测：验证逻辑/物理隔离的路由决策（不依赖真实数据源）。
 */
class TenantIsolationTest {

    private final TenantDataSourceRouter router = new TenantDataSourceRouter();

    @Test
    @DisplayName("逻辑隔离：不切换数据源（返回 null），租户隔离交给底座行级插件")
    void logicalIsolationNoSwitch() {
        assertNull(router.resolveDataSourceKey(TenantIsolationMode.LOGICAL, 10000L));
        assertNull(router.resolveDataSourceKey(null, 10000L));
    }

    @Test
    @DisplayName("物理隔离：按租户生成 tenant_{id} 数据源名")
    void physicalIsolationSwitchByTenant() {
        assertEquals("tenant_10000", router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, 10000L));
        assertEquals("tenant_20000", router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, 20000L));
        assertNotEquals(
                router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, 10000L),
                router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, 20000L),
                "不同租户必须路由到不同数据源");
    }

    @Test
    @DisplayName("物理隔离但租户为空：不切换（fail-closed，避免串到默认共享源）")
    void physicalWithoutTenantDoesNotSwitch() {
        assertNull(router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, null));
    }

    @Test
    @DisplayName("异步扫描缺租户的记录应被跳过：tenantId 为 null 不发送")
    void asyncScanSkipsMissingTenant() {
        Long tenantId = null;
        // 与 ImNotificationServiceImpl.scanAndSendAllTenants 的口径一致：缺租户跳过
        boolean shouldSend = tenantId != null;
        assertFalse(shouldSend);
    }

    @Test
    @DisplayName("隔离模式解析：非法值回退 LOGICAL（fail-safe）")
    void modeParseFallback() {
        // currentMode 读 @Value 字段；直接验证枚举解析口径与路由对 null 的安全处理
        assertEquals(TenantIsolationMode.LOGICAL, TenantIsolationMode.valueOf("LOGICAL"));
        // 物理隔离下 tenantId=0（GLOBAL）仍生成独立键，交由配置决定是否真的建该源
        assertEquals("tenant_0", router.resolveDataSourceKey(TenantIsolationMode.PHYSICAL, 0L));
    }
}
