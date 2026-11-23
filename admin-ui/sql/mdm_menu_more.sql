-- 增量菜单：设备分组、策略管理（追加到 mdm_menu.sql 之后）
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
(9010, '设备分组', 'mdm:group:query', 2, 2, 9000, 'group', 'ep:tree', 'mdm/group/index', 'MdmGroup', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9011, '分组-查询', 'mdm:group:query', 3, 1, 9010, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9012, '分组-新增', 'mdm:group:create', 3, 2, 9010, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9013, '分组-更新', 'mdm:group:update', 3, 3, 9010, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9014, '分组-删除', 'mdm:group:delete', 3, 4, 9010, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9020, '策略管理', 'mdm:policy:query', 2, 3, 9000, 'policy', 'ep:setting', 'mdm/policy/index', 'MdmPolicy', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9021, '策略-查询', 'mdm:policy:query', 3, 1, 9020, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9022, '策略-新增', 'mdm:policy:create', 3, 2, 9020, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9023, '策略-更新', 'mdm:policy:update', 3, 3, 9020, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9024, '策略-删除', 'mdm:policy:delete', 3, 4, 9020, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0');

INSERT INTO system_role_menu (role_id, menu_id) VALUES
(1, 9010), (1, 9011), (1, 9012), (1, 9013), (1, 9014),
(1, 9020), (1, 9021), (1, 9022), (1, 9023), (1, 9024);
