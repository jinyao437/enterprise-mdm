package cn.enterprise.mdm.guard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 租户安全加固单测。
 *
 * <p>重点覆盖独立评审 H-1 / H-2 的回归场景，确保缺陷修复可被自动验证。
 */
class TenantGuardTest {

    @Test
    @DisplayName("H-1 回归：tenantId > 127（超出 Long 缓存）相等必须放行")
    void h1_largeTenantIdEquals_shouldPass() {
        // Long 缓存仅覆盖 -128~127；用 new Long 强制生成不同对象，== 必失败，Objects.equals 应通过
        Long resource = new Long(10000L);
        Long principal = new Long(10000L);
        assertNotSame(resource, principal, "确保是两个不同对象，才能暴露 == 引用比较缺陷");
        assertDoesNotThrow(() -> TenantGuard.assertSameTenant(resource, principal));
        assertTrue(TenantGuard.tenantEquals(resource, principal));
    }

    @Test
    @DisplayName("H-1：跨租户不一致必须拒绝（fail-closed）")
    void h1_differentTenant_shouldDeny() {
        Long resource = 10000L;
        Long principal = 20000L;
        assertThrows(TenantAccessDeniedException.class,
                () -> TenantGuard.assertSameTenant(resource, principal));
        assertFalse(TenantGuard.tenantEquals(resource, principal));
    }

    @Test
    @DisplayName("H-1：任一侧为 null 视为不一致，拒绝")
    void h1_nullTenant_shouldDeny() {
        assertThrows(TenantAccessDeniedException.class,
                () -> TenantGuard.assertSameTenant(null, 10000L));
        assertThrows(TenantAccessDeniedException.class,
                () -> TenantGuard.assertSameTenant(10000L, null));
        // tenantEquals 用 Objects.equals：null 与 null 值语义相等，但 assertSameTenant 对 null 一律拒绝（fail-closed）
        assertTrue(TenantGuard.tenantEquals(null, null));
    }

    @Test
    @DisplayName("H-2 回归：租户上下文缺失必须抛异常，禁止回落默认数据源")
    void h2_missingContext_shouldFailClosed() {
        assertThrows(TenantContextMissingException.class,
                () -> TenantGuard.determineDataSourceKey(null));
    }

    @Test
    @DisplayName("H-2：有上下文时返回租户专属数据源键")
    void h2_withContext_shouldReturnTenantDataSource() {
        assertEquals("ds_tenant_10000", TenantGuard.determineDataSourceKey(10000L));
    }

    @Test
    @DisplayName("H-1：IM 幂等键包含租户维度，跨租户不重叠")
    void imIdempotencyKey_shouldIncludeTenant() {
        String keyA = TenantGuard.buildImIdempotencyKey(10000L, "event-1", "user-1");
        String keyB = TenantGuard.buildImIdempotencyKey(20000L, "event-1", "user-1");
        assertEquals("10000:event-1:user-1", keyA);
        assertNotEquals(keyA, keyB, "不同租户相同 eventId 不得产生相同幂等键");
    }

    @Test
    @DisplayName("H-1：构建幂等键时租户为空应拒绝")
    void imIdempotencyKey_nullTenant_shouldDeny() {
        assertThrows(TenantContextMissingException.class,
                () -> TenantGuard.buildImIdempotencyKey(null, "event-1", "user-1"));
    }
}
