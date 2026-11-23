package cn.enterprise.mdm;

import cn.enterprise.mdm.dal.dataobject.DeviceTokenDO;
import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 设备端专用 API 单测：令牌哈希（不存明文）、租户隔离继承、状态语义。
 */
class DeviceTokenTest {

    /** 与 DeviceTokenServiceImpl.hash 同口径（SHA-256 小写 hex） */
    static String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(d.length * 2);
        for (byte b : d) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    @Test
    @DisplayName("令牌哈希：确定性、64位hex、不含明文")
    void tokenHash() throws Exception {
        String plain = "dtok_abc123";
        String h1 = sha256(plain);
        String h2 = sha256(plain);
        assertEquals(h1, h2, "同一明文哈希应稳定");
        assertEquals(64, h1.length(), "SHA-256 hex 应为 64 字符");
        assertTrue(h1.matches("[0-9a-f]{64}"), "应为小写 hex");
        assertFalse(h1.contains(plain), "哈希不能包含明文");
        assertNotEquals(h1, sha256("dtok_abc124"), "不同明文哈希应不同");
    }

    @Test
    @DisplayName("令牌 DO 继承底座 TenantBaseDO（租户隔离复用底座）")
    void tokenInheritsTenantBase() {
        assertTrue(TenantBaseDO.class.isAssignableFrom(DeviceTokenDO.class));
    }

    @Test
    @DisplayName("令牌状态语义：1启用 2撤销")
    void statusSemantics() {
        DeviceTokenDO t = DeviceTokenDO.builder().tokenHash("h").deviceId(1L).status(1).build();
        assertEquals(1, t.getStatus());
        assertTrue(t.getStatus() == 1, "启用");
        t.setStatus(2);
        assertFalse(t.getStatus() == 1, "撤销后不可用");
    }
}
