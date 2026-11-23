package com.mdm.tenant.guard;

/**
 * 租户上下文缺失异常。
 *
 * <p>当请求未携带有效租户标识时抛出，用于实现 fail-closed：宁可拒绝请求，
 * 也不静默回落到默认数据源，避免跨租户数据污染（独立评审 H-2）。
 */
public class TenantContextMissingException extends RuntimeException {

    public TenantContextMissingException(String message) {
        super(message);
    }
}
