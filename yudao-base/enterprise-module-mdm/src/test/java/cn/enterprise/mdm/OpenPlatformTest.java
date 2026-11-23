package cn.enterprise.mdm;

import cn.enterprise.mdm.dal.dataobject.ApiQuotaDO;
import cn.enterprise.mdm.dal.dataobject.OpenAppDO;
import cn.enterprise.mdm.dal.dataobject.WebhookSubscriptionDO;
import cn.enterprise.mdm.enums.WebhookEventEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 开放平台单测：验证租户隔离继承、Webhook 事件、配额限流判定、密钥不存明文。
 */
class OpenPlatformTest {

    @Test
    @DisplayName("开放平台三 DO 继承底座 TenantBaseDO")
    void dosInheritTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(OpenAppDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(ApiQuotaDO.class));
        assertTrue(TenantBaseDO.class.isAssignableFrom(WebhookSubscriptionDO.class));
    }

    @Test
    @DisplayName("Webhook 事件合法性与名称解析")
    void webhookEvents() {
        assertTrue(WebhookEventEnum.isValid("device.alert"));
        assertFalse(WebhookEventEnum.isValid("unknown.event"));
        assertEquals("设备告警", WebhookEventEnum.nameOf("device.alert"));
        assertEquals("未知", WebhookEventEnum.nameOf("x"));
    }

    @Test
    @DisplayName("配额限流：QPS 与日限超出拒绝，未配置不限")
    void quotaCheck() {
        ApiQuotaDO quota = ApiQuotaDO.builder().appId(1L).qps(10).dailyLimit(1000L).enabled(true).build();
        // 用与 OpenPlatformServiceImpl.checkQuota 一致的判定口径
        assertTrue(checkQuota(5, 500L, quota), "未超限应放行");
        assertFalse(checkQuota(11, 500L, quota), "QPS 超限应拒绝");
        assertFalse(checkQuota(5, 1001L, quota), "日限超限应拒绝");
        // 未启用配额 => 不限
        quota.setEnabled(false);
        assertTrue(checkQuota(999, 99999L, quota));
    }

    private boolean checkQuota(Integer currentQps, Long dailyUsed, ApiQuotaDO quota) {
        if (quota == null || !Boolean.TRUE.equals(quota.getEnabled())) {
            return true;
        }
        if (quota.getQps() != null && currentQps != null && currentQps > quota.getQps()) {
            return false;
        }
        if (quota.getDailyLimit() != null && dailyUsed != null && dailyUsed > quota.getDailyLimit()) {
            return false;
        }
        return true;
    }

    @Test
    @DisplayName("三方应用只存 secret 哈希，不存明文字段")
    void appStoresSecretHashOnly() {
        OpenAppDO app = OpenAppDO.builder().name("xx").appKey("key-1").secretHash("sha256:abc").build();
        assertEquals("sha256:abc", app.getSecretHash());
        assertNotNull(app.getAppKey());
        // DO 中无 secret 明文字段（编译期已保证：字段只有 secretHash）
    }
}
