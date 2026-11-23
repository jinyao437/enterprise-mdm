package com.mdm.security;

import java.util.Objects;

/**
 * M-1 修复：IM 通知幂等键必须包含租户维度。
 *
 * <p>原设计 key = eventId:recipient，SHARED（逻辑隔离）档多租户共用一张
 * im_notification_log 表，若两租户 eventId 命名空间重叠，唯一键冲突会静默丢弃
 * 第二条通知，违反「联动消息必达」（AC-FR8-03）。
 *
 * <p>修复：幂等键改为 tenantId:eventId:recipient；配套唯一约束改为
 * (tenant_id, idempotency_key)，见 resources/sql/im_notification_log.sql。
 */
public final class ImIdempotencyKey {

    private ImIdempotencyKey() {
    }

    /**
     * 构建带租户维度的幂等键。
     *
     * @param tenantId  租户编号，必填
     * @param eventId   事件编号（建议全局唯一，如 UUID / 雪花）
     * @param recipient 接收人
     * @return 幂等键 {@code tenantId:eventId:recipient}
     */
    public static String of(Long tenantId, String eventId, String recipient) {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId 不能为空");
        }
        if (eventId == null || eventId.isEmpty()) {
            throw new IllegalArgumentException("eventId 不能为空");
        }
        if (recipient == null || recipient.isEmpty()) {
            throw new IllegalArgumentException("recipient 不能为空");
        }
        return tenantId + ":" + eventId + ":" + recipient;
    }

    /** 供测试/校验用：两侧幂等键是否相等（值语义） */
    public static boolean equalsKey(String a, String b) {
        return Objects.equals(a, b);
    }
}
