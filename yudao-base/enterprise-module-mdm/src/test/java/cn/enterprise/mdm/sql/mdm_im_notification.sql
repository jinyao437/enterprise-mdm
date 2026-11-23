-- MDM IM 业务通知日志表（本地消息表，保业务联动消息必达）。
-- tenant_id 由底座租户插件自动注入；幂等键含租户维度规避跨租户丢通知（评审 M-1）。
CREATE TABLE IF NOT EXISTS mdm_im_notification_log (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id       BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    idempotency_key VARCHAR(256) NOT NULL COMMENT '幂等键 tenantId:eventId:recipient',
    event_id        VARCHAR(128) NOT NULL COMMENT '业务事件编号',
    message_type    TINYINT      NOT NULL COMMENT '1审批 2设备告警 3策略变更 4合规告警 5应用分发',
    recipient       BIGINT       NOT NULL COMMENT '接收人（平台用户编号）',
    title           VARCHAR(256) DEFAULT NULL,
    content         TEXT         DEFAULT NULL,
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '0待发送 1已送达 2失败',
    retry_count     INT          NOT NULL DEFAULT 0 COMMENT '重试次数',
    im_message_id   VARCHAR(128) DEFAULT NULL COMMENT 'IM 侧消息 id',
    creator         VARCHAR(64)  DEFAULT NULL,
    create_time     DATETIME     DEFAULT NULL,
    updater         VARCHAR(64)  DEFAULT NULL,
    update_time     DATETIME     DEFAULT NULL,
    deleted         BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_idem (tenant_id, idempotency_key),
    KEY idx_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM IM 业务通知日志表';
