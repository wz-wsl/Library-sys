-- 在图书菜单和借阅菜单创建后执行。公开注册使用固定权限字符 reader。
START TRANSACTION;

INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly,
                      status, create_by, create_time, remark)
SELECT '读者', 'reader', 10, '5', 0, '0', 'admin', NOW(), '自助注册的默认角色'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'reader' AND del_flag = '0');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r CROSS JOIN sys_menu m
WHERE r.role_key = 'reader' AND r.del_flag = '0' AND r.status = '0' AND m.status = '0'
  AND ((m.menu_type = 'M' AND m.parent_id = 0 AND m.path = 'library')
       OR m.perms IN ('library:book:list', 'library:category:list', 'library:borrow:list',
                      'library:borrow:add', 'library:borrow:return'))
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

COMMIT;
