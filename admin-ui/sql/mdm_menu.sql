-- 企业 MDM 管理后台菜单（芋道动态路由，从 system_menu 注册）
-- component 指向 src/views/mdm/device/index.vue；菜单 id 用 9000 段避免与底座冲突
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
(9000, 'MDM 设备管理', '', 1, 90, 0, '/mdm', 'ep:monitor', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9001, '设备列表', 'mdm:device:query', 2, 1, 9000, 'device', 'ep:list', 'mdm/device/index', 'MdmDevice', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9002, '设备-查询', 'mdm:device:query', 3, 1, 9001, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9003, '设备-注册', 'mdm:device:create', 3, 2, 9001, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9004, '设备-更新', 'mdm:device:update', 3, 3, 9001, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9005, '设备-删除', 'mdm:device:delete', 3, 4, 9001, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0');

-- 赋予超级管理员角色（role_id=1）全部 MDM 菜单权限
INSERT INTO system_role_menu (role_id, menu_id) VALUES
(1, 9000), (1, 9001), (1, 9002), (1, 9003), (1, 9004), (1, 9005);
