package com.mdm.tenant.guard;

/**
 * 跨租户访问拒绝异常。
 *
 * <p>当资源所属租户与当前主体租户不一致时抛出，防止越权读写其他租户数据
 * （独立评审 H-1）。
 */
public class TenantAccessDeniedException extends RuntimeException {

    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
