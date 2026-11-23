package cn.enterprise.mdm.datasource;

/**
 * 租户隔离模式：对应 PRD 的「逻辑隔离 / 物理隔离」。
 *
 * <p>逻辑隔离：共享库 + tenant_id 行级隔离（芋道底座原生能力，默认）。
 * 物理隔离：按租户切换独立数据源（独立库/Schema），由
 * {@link TenantDataSourceRouter} 结合 dynamic-datasource 实现。
 */
public enum TenantIsolationMode {

    LOGICAL,
    PHYSICAL
}
