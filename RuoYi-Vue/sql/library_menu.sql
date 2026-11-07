-- 图书管理目录、图书信息和图书分类菜单，以及它们的按钮权限。
-- 在 library.sql 之后、library_borrow_menu.sql 之前执行，可以重复执行。
-- 不写死菜单编号，按 path / component / perms 判断是否已存在。
START TRANSACTION;

-- 1. 顶部目录「图书管理」
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, perms, icon, create_by, create_time)
SELECT '图书管理', 0, 5, 'library', 'M', '', 'education', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE parent_id = 0 AND path = 'library' AND menu_type = 'M');

-- 2. 「图书信息」和「图书分类」两个页面菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, perms, icon, create_by, create_time)
SELECT '图书信息', p.menu_id, 1, 'book', 'library/book/index', '', 'C', 'library:book:list', 'education', 'admin', NOW()
FROM sys_menu p WHERE p.parent_id = 0 AND p.path = 'library' AND p.menu_type = 'M'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.component = 'library/book/index');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name, menu_type, perms, icon, create_by, create_time)
SELECT '图书分类', p.menu_id, 2, 'category', 'library/category/index', 'LibCategory', 'C', 'library:category:list', 'education', 'admin', NOW()
FROM sys_menu p WHERE p.parent_id = 0 AND p.path = 'library' AND p.menu_type = 'M'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.component = 'library/category/index');

-- 3. 图书信息的按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, perms, create_by, create_time)
SELECT b.menu_name, p.menu_id, b.order_num, 'F', b.perms, 'admin', NOW()
FROM sys_menu p
JOIN (SELECT '新增' AS menu_name, 1 AS order_num, 'library:book:add' AS perms
      UNION ALL SELECT '修改', 2, 'library:book:edit'
      UNION ALL SELECT '查询', 3, 'library:book:query'
      UNION ALL SELECT '删除', 4, 'library:book:remove') b
WHERE p.component = 'library/book/index' AND p.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = b.perms);

-- 4. 图书分类的按钮
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, perms, create_by, create_time)
SELECT b.menu_name, p.menu_id, b.order_num, 'F', b.perms, 'admin', NOW()
FROM sys_menu p
JOIN (SELECT '分类新增' AS menu_name, 1 AS order_num, 'library:category:add' AS perms
      UNION ALL SELECT '分类修改', 2, 'library:category:edit'
      UNION ALL SELECT '分类查询', 3, 'library:category:query'
      UNION ALL SELECT '分类删除', 4, 'library:category:remove'
      UNION ALL SELECT '分类移动', 5, 'library:category:move') b
WHERE p.component = 'library/category/index' AND p.menu_type = 'C'
AND NOT EXISTS (SELECT 1 FROM sys_menu m WHERE m.perms = b.perms);

COMMIT;
