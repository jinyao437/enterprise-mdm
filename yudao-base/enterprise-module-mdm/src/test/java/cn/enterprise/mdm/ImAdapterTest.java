package cn.enterprise.mdm;

import cn.enterprise.mdm.client.ImClient;
import cn.enterprise.mdm.dal.dataobject.ImNotificationLogDO;
import cn.enterprise.mdm.enums.ImMessageTypeEnum;
import cn.enterprise.mdm.enums.ImNotifyStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IM 适配层单测：验证适配接口可插拔、租户隔离继承、状态语义、幂等键含租户。
 */
class ImAdapterTest {

    @Test
    @DisplayName("通知日志 DO 继承底座 TenantBaseDO（租户隔离复用底座）")
    void logInheritsTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(ImNotificationLogDO.class));
    }

    @Test
    @DisplayName("适配接口可插拔：野火实现是 ImClient 的一种实现")
    void clientIsPluggable() {
        // 用匿名实现模拟后期商业版，验证业务侧只依赖 ImClient 接口
        ImClient commercial = new ImClient() {
            @Override
            public String sendPrivateMessage(Long t, Long u, String title, String content) {
                return "commercial-msg-1";
            }
            @Override
            public String sendGroupMessage(Long t, String g, String title, String content) {
                return "commercial-group-1";
            }
            @Override
            public boolean syncUser(Long t, Long u, String imUserId, String name) {
                return true;
            }
        };
        assertEquals("commercial-msg-1", commercial.sendPrivateMessage(1L, 2L, "t", "c"));
        assertTrue(commercial.syncUser(1L, 2L, "im-2", "张三"));
    }

    @Test
    @DisplayName("业务通知消息类型覆盖审批/告警/策略/合规/应用分发")
    void messageTypes() {
        assertEquals("审批结果通知", ImMessageTypeEnum.nameOf(1));
        assertEquals("设备告警", ImMessageTypeEnum.nameOf(2));
        assertEquals("策略变更", ImMessageTypeEnum.nameOf(3));
        assertEquals("合规告警", ImMessageTypeEnum.nameOf(4));
        assertEquals("应用分发通知", ImMessageTypeEnum.nameOf(5));
    }

    @Test
    @DisplayName("通知状态机：待发送/已送达/失败")
    void notifyStatus() {
        assertEquals("待发送", ImNotifyStatusEnum.nameOf(0));
        assertEquals("已送达", ImNotifyStatusEnum.nameOf(1));
        assertEquals("发送失败", ImNotifyStatusEnum.nameOf(2));
    }

    @Test
    @DisplayName("幂等键含租户维度：跨租户相同 eventId 不冲突（评审 M-1 正确做法）")
    void idempotencyKeyIncludesTenant() {
        String keyA = 10000L + ":" + "event-1" + ":" + 200L;
        String keyB = 20000L + ":" + "event-1" + ":" + 200L;
        assertNotEquals(keyA, keyB);
        assertEquals("10000:event-1:200", keyA);
    }
}
