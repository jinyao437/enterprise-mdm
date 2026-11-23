-- 设备端专用 API 的访问令牌表（设备 agent 鉴权用，非管理端 OAuth2）
-- tenant_id 由底座插件自动注入；令牌 hash 唯一且含租户维度
CREATE TABLE IF NOT EXISTS mdm_device_token (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id      BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    token_hash     VARCHAR(64)  NOT NULL COMMENT '令牌 SHA-256 哈希，明文不入库',
    device_id      BIGINT       NOT NULL COMMENT '绑定的设备编号',
    identifier     VARCHAR(256) DEFAULT NULL COMMENT '设备标识（IMEI/SN）',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 2撤销',
    expire_time    DATETIME     DEFAULT NULL COMMENT '过期时间，NULL 不过期',
    last_used_time DATETIME     DEFAULT NULL COMMENT '最近使用时间',
    creator        VARCHAR(64)  DEFAULT NULL,
    create_time    DATETIME     DEFAULT NULL,
    updater        VARCHAR(64)  DEFAULT NULL,
    update_time    DATETIME     DEFAULT NULL,
    deleted        BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_token (tenant_id, token_hash),
    KEY idx_tenant_device (tenant_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 设备访问令牌表';
