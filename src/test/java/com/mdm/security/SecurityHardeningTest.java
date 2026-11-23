package com.mdm.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 中危加固（M-1~M-4）单测。 */
class SecurityHardeningTest {

    @Test
    @DisplayName("M-1：幂等键含租户维度，跨租户相同 eventId 不冲突")
    void m1_idempotencyKeyIncludesTenant() {
        String a = ImIdempotencyKey.of(10000L, "event-1", "user-1");
        String b = ImIdempotencyKey.of(20000L, "event-1", "user-1");
        assertEquals("10000:event-1:user-1", a);
        assertNotEquals(a, b, "不同租户相同 eventId 不得产生相同幂等键");
        assertThrows(IllegalArgumentException.class, () -> ImIdempotencyKey.of(null, "e", "u"));
    }

    @Test
    @DisplayName("M-2：WIPE 令牌缺失或不匹配必须拒绝，非 WIPE 放行")
    void m2_wipeConfirmToken() {
        assertTrue(WipeConfirmGuard.verifyBeforeDispatch("LOCK", null, null));
        assertTrue(WipeConfirmGuard.verifyBeforeDispatch("WIPE", "tok-1", "tok-1"));
        assertThrows(IllegalStateException.class,
                () -> WipeConfirmGuard.verifyBeforeDispatch("WIPE", null, "tok-1"));
        assertThrows(IllegalStateException.class,
                () -> WipeConfirmGuard.verifyBeforeDispatch("WIPE", "tok-1", "wrong"));
    }

    @Test
    @DisplayName("M-3：密钥不再以明文字面量入库，可还原")
    void m3_secretProtected() {
        String plain = "whsec_abcdef123456";
        String stored = SecretProtector.protect(plain);
        assertTrue(SecretProtector.isProtected(stored));
        assertNotEquals(plain, stored, "存储值不得等于明文");
        assertEquals(plain, SecretProtector.reveal(stored));
        assertThrows(IllegalArgumentException.class, () -> SecretProtector.reveal(plain));
    }

    @Test
    @DisplayName("M-4：审计哈希链 - 篡改中间记录可被发现")
    void m4_auditHashChainDetectsTamper() {
        AuditHashChain chain = AuditHashChain.newChain();
        String h1 = chain.append("op=login;user=u1");
        String h2 = chain.append("op=wipe;device=d1");
        String h3 = chain.append("op=logout;user=u1");

        // 独立重算：原记录校验一致
        assertEquals(h1, AuditHashChain.hashOf(AuditHashChain.GENESIS, "op=login;user=u1"));
        assertEquals(h2, AuditHashChain.hashOf(h1, "op=wipe;device=d1"));
        assertEquals(h3, AuditHashChain.hashOf(h2, "op=logout;user=u1"));

        // 篡改：把第二条记录内容改掉，重算 hash 与原链不符 —— 篡改可被发现
        String tamperedRecompute = AuditHashChain.hashOf(h1, "op=wipe;device=d2");
        assertNotEquals(h2, tamperedRecompute, "被篡改记录重算 hash 应与原 hash 不符");
    }
}
