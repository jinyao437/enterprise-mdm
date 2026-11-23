-- MDM 设备表（增量表，芋道底座无此表）
-- tenant_id 由芋道多租户插件自动注入，本模块不写任何租户拦截代码
CREATE TABLE IF NOT EXISTS mdm_device (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '设备编号',
    tenant_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    identifier   VARCHAR(128) NOT NULL COMMENT '设备唯一标识 IMEI/SN/UUID',
    name         VARCHAR(128) NOT NULL COMMENT '设备名称',
    platform     VARCHAR(32)  NOT NULL COMMENT '平台 android/ios/harmony/windows/macos',
    os_version   VARCHAR(64)  DEFAULT NULL COMMENT '系统版本',
    status       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态 0已注册 1使用中 2已挂失 3已停用 4已报废',
    bind_user_id BIGINT       DEFAULT NULL COMMENT '绑定用户编号（芋道系统用户）',
    group_id     BIGINT       DEFAULT NULL COMMENT '设备分组编号',
    creator      VARCHAR(64)  DEFAULT NULL,
    create_time  DATETIME     DEFAULT NULL,
    updater      VARCHAR(64)  DEFAULT NULL,
    update_time  DATETIME     DEFAULT NULL,
    deleted      BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_identifier (tenant_id, identifier),
    KEY idx_tenant_status (tenant_id, status),
    KEY idx_tenant_group (tenant_id, group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 设备表';
