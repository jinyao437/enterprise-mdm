-- MDM 设备分组表（树形）。tenant_id 由底座租户插件自动注入。
CREATE TABLE IF NOT EXISTS mdm_device_group (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    name        VARCHAR(128) NOT NULL COMMENT '分组名称',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分组编号，根为0',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序',
    remark      VARCHAR(512) DEFAULT NULL COMMENT '备注',
    creator     VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    updater     VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    deleted     BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_tenant_parent (tenant_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 设备分组表';

-- MDM 合规上报表。不合规上报进入待告警队列，告警后标记 alerted=1。
CREATE TABLE IF NOT EXISTS mdm_compliance_report (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tenant_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号（底座自动注入）',
    device_id   BIGINT       NOT NULL COMMENT '设备编号',
    check_item  VARCHAR(64)  NOT NULL COMMENT '检查项',
    status      TINYINT      NOT NULL COMMENT '0合规 1不合规 2待复核',
    detail      TEXT         COMMENT '检查详情 JSON',
    alerted     BIT(1)       NOT NULL DEFAULT b'0' COMMENT '是否已告警',
    report_time DATETIME     DEFAULT NULL COMMENT '上报时间',
    creator     VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    updater     VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    deleted     BIT(1)       NOT NULL DEFAULT b'0',
    PRIMARY KEY (id),
    KEY idx_tenant_device_item (tenant_id, device_id, check_item),
    KEY idx_tenant_alert (tenant_id, status, alerted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MDM 设备合规上报表';
