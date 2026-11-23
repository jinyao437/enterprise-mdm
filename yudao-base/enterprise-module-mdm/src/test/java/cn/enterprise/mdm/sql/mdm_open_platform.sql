-- MDM 开放平台三表。tenant_id 由底座租户插件自动注入。
CREATE TABLE IF NOT EXISTS mdm_open_app (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    name             VARCHAR(128) NOT NULL COMMENT '应用名称',
    app_key          VARCHAR(128) NOT NULL COMMENT 'appKey（对应底座 OAuth2 clientId）',
    secret_hash      VARCHAR(128) NOT NULL COMMENT 'secret 哈希，不存明文',
    status           TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 2停用',
    description      VARCHAR(512) DEFAULT NULL,
    oauth2_client_id BIGINT       DEFAULT NULL COMMENT '底座 OAuth2 client 编号',
    creator          VARCHAR(64)  DEFAULT NULL,
    create_time      DATETIME     DEFAULT NULL,
    updater          VARCHAR(64)  DEFAULT NULL,
    update_time      DATETIME     DEFAULT NULL,
    deleted          BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_appkey (tenant_id, app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 开放平台应用表';

CREATE TABLE IF NOT EXISTS mdm_api_quota (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT       NOT NULL DEFAULT 0,
    app_id      BIGINT       NOT NULL COMMENT '应用编号',
    api_pattern VARCHAR(128) NOT NULL COMMENT '接口标识，* 表示全部',
    qps         INT          DEFAULT NULL COMMENT '每秒上限',
    daily_limit BIGINT       DEFAULT NULL COMMENT '每日上限',
    enabled     BIT(1)       NOT NULL DEFAULT b'1',
    creator     VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    updater     VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    deleted     BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_app_api (tenant_id, app_id, api_pattern)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM API 配额表';

CREATE TABLE IF NOT EXISTS mdm_webhook_subscription (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id      BIGINT       NOT NULL DEFAULT 0,
    app_id         BIGINT       NOT NULL,
    event          VARCHAR(64)  NOT NULL COMMENT '订阅事件',
    url            VARCHAR(1024) NOT NULL COMMENT '回调地址',
    secret_enc     VARCHAR(512) DEFAULT NULL COMMENT '签名密钥密文（生产接 KMS）',
    enabled        BIT(1)       NOT NULL DEFAULT b'1',
    last_push_time DATETIME     DEFAULT NULL,
    creator        VARCHAR(64)  DEFAULT NULL,
    create_time    DATETIME     DEFAULT NULL,
    updater        VARCHAR(64)  DEFAULT NULL,
    update_time    DATETIME     DEFAULT NULL,
    deleted        BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_app_event (tenant_id, app_id, event)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM Webhook 订阅表';
