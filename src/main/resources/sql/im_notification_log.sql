-- M-1 修复配套 DDL：IM 通知日志唯一约束改为 (tenant_id, idempotency_key)
-- 避免 SHARED 档多租户 eventId 命名空间重叠时静默丢通知。
CREATE TABLE IF NOT EXISTS im_notification_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL COMMENT '租户编号（隔离维度）',
    idempotency_key VARCHAR(256) NOT NULL COMMENT '幂等键 = tenantId:eventId:recipient',
    event_id        VARCHAR(128) NOT NULL,
    recipient       VARCHAR(128) NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    created_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    -- 修复点：唯一键包含 tenant_id，跨租户不再冲突
    UNIQUE KEY uk_idem_tenant (tenant_id, idempotency_key),
    KEY idx_tenant_event (tenant_id, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM 通知发送日志（租户隔离幂等）';

-- Webhook 密钥存储字段改造示意（M-3）：明文 secret 改为密文列，禁止明文入库
-- ALTER TABLE webhook_subscription
--   ADD COLUMN secret_enc VARCHAR(512) NOT NULL COMMENT 'KMS/信封加密后的密文',
--   DROP COLUMN secret;
