package cn.enterprise.mdm.guard;

import java.util.Objects;

/**
 * 租户安全加固工具类。
 *
 * <p>修复企业 MDM 多租户平台独立评审发现的两项高危缺陷：
 * <ul>
 *   <li><b>H-1</b>：跨租户核心校验原先使用 {@code ==} 比较包装类型 Long/String，
 *       直译为 Java 会因引用比较导致合法请求 403 或删除校验后 fail-open 越权。
 *       本类统一改用 {@link Objects#equals(Object, Object)} 进行值比较。</li>
 *   <li><b>H-2</b>：数据源路由原先在租户上下文缺失时 {@code orElse(DEFAULT_KEY)}
 *       静默回落到默认（shared）数据源，导致跨租户污染。本类改为 fail-closed，
 *       缺上下文直接抛 {@link TenantContextMissingException}。</li>
 * </ul>
 *
 * <p>本类为无状态静态工具，不依赖芋道底座运行时，便于在纯 JDK 环境编译与单测。
 */
public final class TenantGuard {

    /** 对外暴露的默认租户判断，仅用于显式声明「不自动回落」 */
    private static final Long NO_TENANT = null;

    private TenantGuard() {
    }

    /**
     * H-1 修复：跨租户一致性校验，统一使用 Objects.equals 做值比较。
     *
     * <p>覆盖 tenantId &gt; 127（超出 Long 缓存）场景，避免 {@code ==} 引用比较误判。
     * 任一侧为 null 均视为不一致并拒绝（fail-closed）。
     *
     * @param resourceTenantId 资源所属租户
     * @param principalTenantId 当前主体租户
     * @throws TenantAccessDeniedException 两侧不相等时抛出
     */
    public static void assertSameTenant(Long resourceTenantId, Long principalTenantId) {
        if (!Objects.equals(resourceTenantId, principalTenantId)) {
            throw new TenantAccessDeniedException(
                    "跨租户访问被拒绝: resourceTenantId=" + resourceTenantId
                            + ", principalTenantId=" + principalTenantId);
        }
    }

    /**
     * H-1 修复：供 IM 回调等场景使用的通用相等判断（值语义，null 安全）。
     */
    public static boolean tenantEquals(Long a, Long b) {
        return Objects.equals(a, b);
    }

    /**
     * H-2 修复：数据源路由键决策，缺上下文 fail-closed。
     *
     * <p>原设计在 TenantContextHolder 为空时回落到 DEFAULT_KEY（shared 源），
     * 拦截器遗漏（原生 SQL / 定时任务 / MQ 回调 / 异步线程）时会读写共享库，
     * 造成跨租户污染。这里改为：上下文缺失直接抛异常，禁止回落默认源。
     *
     * @param contextTenantId 当前租户上下文（可能为 null）
     * @return 数据源路由键，格式为 {@code ds_tenant_{tenantId}}
     * @throws TenantContextMissingException 上下文缺失时抛出
     */
    public static String determineDataSourceKey(Long contextTenantId) {
        if (contextTenantId == null || Objects.equals(contextTenantId, NO_TENANT)) {
            throw new TenantContextMissingException(
                    "租户上下文缺失，禁止回落默认数据源（fail-closed）");
        }
        return "ds_tenant_" + contextTenantId;
    }

    /**
     * H-1 修复：IM 通知幂等键必须包含租户维度，避免 SHARED 档跨租户 eventId 重叠丢通知。
     *
     * @param tenantId  租户编号，必填
     * @param eventId   事件编号（建议全局唯一，如 UUID/雪花）
     * @param recipient 接收人
     * @return 幂等键，格式 {@code tenantId:eventId:recipient}
     */
    public static String buildImIdempotencyKey(Long tenantId, String eventId, String recipient) {
        if (tenantId == null) {
            throw new TenantContextMissingException("构建幂等键时租户编号不能为空");
        }
        return tenantId + ":" + eventId + ":" + recipient;
    }
}
