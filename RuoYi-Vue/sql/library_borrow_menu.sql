-- 借阅菜单和按钮权限，可重复执行。
START TRANSACTION;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, is_cache, menu_type, perms, create_by, create_time)
SELECT '我的借阅', p.menu_id, 3, 'borrow', 'library/borrow/index', 'LibBorrow', 1, 'C', 'library:borrow:list', 'admin', NOW()
FROM sys_menu p WHERE p.parent_id = 0 AND p.path = 'library' AND p.menu_type = 'M'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.parent_id = p.menu_id AND m.path = 'borrow');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, perms, create_by, create_time)
SELECT '借书', p.menu_id, 5, 'F', 'library:borrow:add', 'admin', NOW()
FROM sys_menu p WHERE p.component = 'library/book/index' AND p.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = 'library:borrow:add');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, perms, create_by, create_time)
SELECT '还书', p.menu_id, 1, 'F', 'library:borrow:return', 'admin', NOW()
FROM sys_menu p WHERE p.component = 'library/borrow/index' AND p.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = 'library:borrow:return');

COMMIT;
