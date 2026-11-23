package cn.enterprise.mdm.datasource;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 租户数据源路由（物理隔离）。
 *
 * <p>逻辑隔离（默认）完全交给芋道底座的 MyBatis-Plus 租户插件，本路由不介入。
 * 物理隔离模式下，按租户编号切换 dynamic-datasource 的数据源：数据源名约定为
 * {@code tenant_{tenantId}}，需在配置中为每个大客户配置对应数据源（独立库/Schema）。
 *
 * <p>隔离策略对业务代码透明：业务只需在请求入口调用 {@link #activate(Long)} /
 * {@link #clear()}，或由 Web Filter 统一处理。这里提供显式工具与决策逻辑，
 * 便于在无真实多库环境时单测验证路由正确性。
 */
@Component
public class TenantDataSourceRouter {

    /** 数据源名前缀，与配置项 dynamic.datasource.ds.tenant_{id} 对应 */
    public static final String DS_PREFIX = "tenant_";

    @Value("${mdm.tenant.isolation-mode:logical}")
    private String isolationMode;

    /**
     * 依据配置模式，决定给定租户应使用的物理数据源名。
     *
     * @return 物理隔离返回 {@code tenant_{id}}；逻辑隔离返回 null（不切换）
     */
    public String resolveDataSourceKey(TenantIsolationMode mode, Long tenantId) {
        if (mode == null || mode == TenantIsolationMode.LOGICAL || tenantId == null) {
            return null;
        }
        return DS_PREFIX + tenantId;
    }

    /** 当前配置解析为枚举（非法值回退 LOGICAL，fail-safe） */
    public TenantIsolationMode currentMode() {
        if (isolationMode == null) {
            return TenantIsolationMode.LOGICAL;
        }
        try {
            return TenantIsolationMode.valueOf(isolationMode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TenantIsolationMode.LOGICAL;
        }
    }

    /**
     * 激活租户数据源：逻辑隔离不切换；物理隔离 push 对应数据源到上下文。
     * 必须与 {@link #clear()} 成对使用（建议 try/finally）。
     */
    public void activate(Long tenantId) {
        String key = resolveDataSourceKey(currentMode(), tenantId);
        if (key != null) {
            DynamicDataSourceContextHolder.push(key);
        }
    }

    /** 清理当前线程的数据源上下文 */
    public void clear() {
        DynamicDataSourceContextHolder.clear();
    }
}
