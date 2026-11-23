-- MDM 策略表（增量，芋道底座无此表）。tenant_id 由底座租户插件自动注入。
CREATE TABLE IF NOT EXISTS mdm_device_policy (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    name        VARCHAR(128) NOT NULL COMMENT '策略名称',
    type        TINYINT      NOT NULL COMMENT '1安全 2网络 3应用 4内容',
    content     TEXT         COMMENT '策略内容 JSON',
    scope_type  VARCHAR(16)  NOT NULL COMMENT 'GROUP / DEVICE',
    scope_id    BIGINT       NOT NULL COMMENT '分组或设备编号',
    priority    INT          NOT NULL DEFAULT 0 COMMENT '优先级',
    enabled     BIT(1)       NOT NULL DEFAULT b'1',
    creator     VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    updater     VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    deleted     BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_tenant_scope (tenant_id, scope_type, scope_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 设备策略表';

-- MDM 远程指令表。幂等键含租户维度，唯一键 (tenant_id, idempotency_key) 规避跨租户丢指令。
CREATE TABLE IF NOT EXISTS mdm_device_command (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    device_id        BIGINT       NOT NULL COMMENT '目标设备编号',
    type             TINYINT      NOT NULL COMMENT '1锁定 2擦除 3定位 4应用推送 5策略刷新',
    params           TEXT         COMMENT '指令参数 JSON',
    status           TINYINT      NOT NULL DEFAULT 0 COMMENT '0已创建 1已下发 2已确认 3失败',
    idempotency_key  VARCHAR(256) NOT NULL COMMENT '幂等键 tenantId:业务key',
    confirm_token    VARCHAR(128) DEFAULT NULL COMMENT '高危指令二次确认令牌（非数据库id）',
    dispatch_attempts INT         NOT NULL DEFAULT 0 COMMENT '下发次数',
    creator          VARCHAR(64)  DEFAULT NULL,
    create_time      DATETIME     DEFAULT NULL,
    updater          VARCHAR(64)  DEFAULT NULL,
    update_time      DATETIME     DEFAULT NULL,
    deleted          BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_idem (tenant_id, idempotency_key),
    KEY idx_tenant_device (tenant_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 远程指令表';
