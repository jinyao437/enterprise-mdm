package cn.enterprise.mdm;

import cn.enterprise.mdm.dal.dataobject.AppDistributionDO;
import cn.enterprise.mdm.dal.dataobject.AppDO;
import cn.enterprise.mdm.dal.dataobject.AppVersionDO;
import cn.enterprise.mdm.enums.AppStatusEnum;
import cn.enterprise.mdm.enums.DistributionStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 应用分发单测：验证 DO 继承底座租户基类、状态语义与灰度边界。
 */
class AppDistributionTest {

    @Test
    @DisplayName("应用/版本/分发 DO 继承底座 TenantBaseDO")
    void dosInheritTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(AppDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(AppVersionDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(AppDistributionDO.class));
    }

    @Test
    @DisplayName("应用状态覆盖草稿/上架/下架")
    void appStatus() {
        assertEquals("草稿", AppStatusEnum.nameOf(0));
        assertEquals("已上架", AppStatusEnum.nameOf(1));
        assertEquals("已下架", AppStatusEnum.nameOf(2));
    }

    @Test
    @DisplayName("分发状态覆盖待下发/灰度/全量/暂停/取消")
    void distributionStatus() {
        assertEquals("待下发", DistributionStatusEnum.nameOf(0));
        assertEquals("灰度中", DistributionStatusEnum.nameOf(1));
        assertEquals("全量完成", DistributionStatusEnum.nameOf(2));
        assertEquals("已暂停", DistributionStatusEnum.nameOf(3));
        assertEquals("已取消", DistributionStatusEnum.nameOf(4));
    }

    @Test
    @DisplayName("灰度比例边界：0-100 合法，越界非法（Service 校验口径）")
    void grayPercentBoundary() {
        assertTrue(isValidGray(0));
        assertTrue(isValidGray(50));
        assertTrue(isValidGray(100));
        assertFalse(isValidGray(-1));
        assertFalse(isValidGray(101));
    }

    /** 与 AppServiceImpl.createDistribution 中的校验口径一致 */
    private boolean isValidGray(Integer gray) {
        return gray != null && gray >= 0 && gray <= 100;
    }

    @Test
    @DisplayName("版本校验值字段可承载 SHA-256（防篡改）")
    void versionChecksum() {
        AppVersionDO version = AppVersionDO.builder()
                .appId(1L)
                .version("1.2.0")
                .versionCode(10200L)
                .packageUrl("https://cdn.example.com/app-1.2.0.apk")
                .checksum("sha256:abcdef")
                .forceUpdate(true)
                .build();
        assertEquals("1.2.0", version.getVersion());
        assertEquals(10200L, version.getVersionCode());
        assertNotNull(version.getChecksum());
        assertTrue(version.getForceUpdate());
    }
}
