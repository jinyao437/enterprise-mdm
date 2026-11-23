-- 增量菜单：应用分发、合规检查、远程管控、开放平台、IM 通知
INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted) VALUES
(9030, '应用分发', 'mdm:app:query', 2, 4, 9000, 'app', 'ep:box', 'mdm/app/index', 'MdmApp', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9031, '应用-分发', 'mdm:app:distribute', 3, 1, 9030, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9040, '合规检查', 'mdm:compliance:query', 2, 5, 9000, 'compliance', 'ep:circle-check', 'mdm/compliance/index', 'MdmCompliance', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9041, '合规-更新', 'mdm:compliance:update', 3, 1, 9040, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9050, '远程管控', 'mdm:command:query', 2, 6, 9000, 'command', 'ep:operation', 'mdm/command/index', 'MdmCommand', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9051, '指令-下发', 'mdm:command:dispatch', 3, 1, 9050, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9060, '开放平台', 'mdm:open:query', 2, 7, 9000, 'openapi', 'ep:link', 'mdm/openapi/index', 'MdmOpenPlatform', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9061, '开放平台-应用', 'mdm:open:app', 3, 1, 9060, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9062, '开放平台-配额', 'mdm:open:quota', 3, 2, 9060, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9063, '开放平台-Webhook', 'mdm:open:webhook', 3, 3, 9060, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9070, 'IM 通知', 'mdm:im:query', 2, 8, 9000, 'imnotice', 'ep:message', 'mdm/imnotice/index', 'MdmImNotice', 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0'),
(9071, '通知-发送', 'mdm:im:notify', 3, 1, 9070, '', '', NULL, NULL, 0, '1', '1', '1', 'admin', NOW(), '1', NOW(), '0');

INSERT INTO system_role_menu (role_id, menu_id) VALUES
(1, 9030), (1, 9031), (1, 9040), (1, 9041), (1, 9050), (1, 9051),
(1, 9060), (1, 9061), (1, 9062), (1, 9063), (1, 9070), (1, 9071);
