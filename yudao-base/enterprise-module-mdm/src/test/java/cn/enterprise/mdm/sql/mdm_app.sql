-- MDM 应用表。tenant_id 由底座租户插件自动注入。
CREATE TABLE IF NOT EXISTS mdm_app (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    name         VARCHAR(128) NOT NULL COMMENT '应用名称',
    package_name VARCHAR(256) NOT NULL COMMENT '应用包名',
    app_type     VARCHAR(16)  NOT NULL COMMENT 'INTERNAL 企业自建 / THIRD_PARTY 三方接入',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已上架 2已下架',
    icon         VARCHAR(512) DEFAULT NULL COMMENT '图标 URL',
    description  VARCHAR(1024) DEFAULT NULL COMMENT '应用描述',
    open_app_id  VARCHAR(128) DEFAULT NULL COMMENT '三方应用开放平台 appId',
    creator      VARCHAR(64)  DEFAULT NULL,
    create_time  DATETIME     DEFAULT NULL,
    updater      VARCHAR(64)  DEFAULT NULL,
    update_time  DATETIME     DEFAULT NULL,
    deleted      BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_package (tenant_id, package_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 企业应用表';

-- 应用版本表
CREATE TABLE IF NOT EXISTS mdm_app_version (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id    BIGINT       NOT NULL DEFAULT 0,
    app_id       BIGINT       NOT NULL COMMENT '应用编号',
    version      VARCHAR(64)  NOT NULL COMMENT '版本名',
    version_code BIGINT       NOT NULL COMMENT '版本码',
    package_url  VARCHAR(1024) NOT NULL COMMENT '安装包地址',
    checksum     VARCHAR(128) DEFAULT NULL COMMENT 'SHA-256 校验值',
    changelog    VARCHAR(2048) DEFAULT NULL COMMENT '更新说明',
    force_update BIT(1)       NOT NULL DEFAULT b'0',
    creator      VARCHAR(64)  DEFAULT NULL,
    create_time  DATETIME     DEFAULT NULL,
    updater      VARCHAR(64)  DEFAULT NULL,
    update_time  DATETIME     DEFAULT NULL,
    deleted      BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_app_ver (tenant_id, app_id, version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 应用版本表';

-- 分发任务表（灰度）
CREATE TABLE IF NOT EXISTS mdm_app_distribution (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id     BIGINT       NOT NULL DEFAULT 0,
    app_id        BIGINT       NOT NULL,
    version_id    BIGINT       NOT NULL,
    scope_type    VARCHAR(16)  NOT NULL COMMENT 'ALL / GROUP / DEVICE',
    scope_id      BIGINT       NOT NULL DEFAULT 0,
    gray_percent  INT          NOT NULL DEFAULT 100 COMMENT '灰度比例 0-100',
    status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待下发 1灰度中 2全量 3暂停 4取消',
    success_count INT          NOT NULL DEFAULT 0,
    fail_count    INT          NOT NULL DEFAULT 0,
    creator       VARCHAR(64)  DEFAULT NULL,
    create_time   DATETIME     DEFAULT NULL,
    updater       VARCHAR(64)  DEFAULT NULL,
    update_time   DATETIME     DEFAULT NULL,
    deleted       BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 应用分发任务表';
