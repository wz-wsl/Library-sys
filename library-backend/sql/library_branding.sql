-- 把若依自带的演示数据换成图书管理系统自己的信息。
-- 在 ry_20260417.sql 之后执行，可以重复执行。
START TRANSACTION;

-- 1. 删除顶部的「若依官网」外链菜单，以及它和角色的关联
DELETE FROM sys_role_menu WHERE menu_id = 4;
DELETE FROM sys_menu WHERE menu_id = 4 AND path = 'http://ruoyi.vip';

-- 2. 部门：根部门和两个分公司改成图书馆，清掉演示用的负责人、电话、邮箱
UPDATE sys_dept SET dept_name = '图书馆' WHERE dept_id = 100 AND dept_name = '若依科技';
UPDATE sys_dept SET dept_name = '总馆'   WHERE dept_id = 101 AND dept_name = '深圳总公司';
UPDATE sys_dept SET dept_name = '分馆'   WHERE dept_id = 102 AND dept_name = '长沙分公司';
UPDATE sys_dept SET leader = NULL, phone = NULL, email = NULL WHERE leader = '若依';
-- 下面的部门换成图书馆的部门（只改名字，部门编号不变，所以用户的所属部门不受影响）
UPDATE sys_dept SET dept_name = '技术部' WHERE dept_id = 103 AND dept_name = '研发部门';
UPDATE sys_dept SET dept_name = '采编部' WHERE dept_id = 104 AND dept_name = '市场部门';
UPDATE sys_dept SET dept_name = '流通部' WHERE dept_id = 105 AND dept_name = '测试部门';
UPDATE sys_dept SET dept_name = '阅览部' WHERE dept_id = 106 AND dept_name = '财务部门';
UPDATE sys_dept SET dept_name = '办公室' WHERE dept_id = 107 AND dept_name = '运维部门';
UPDATE sys_dept SET dept_name = '流通部' WHERE dept_id = 108 AND dept_name = '市场部门';
UPDATE sys_dept SET dept_name = '阅览部' WHERE dept_id = 109 AND dept_name = '财务部门';
-- 新建「读者」部门，和总馆、分馆平级。注册的读者会自动放进这里（按名字「读者」查找）
INSERT INTO sys_dept (dept_id, parent_id, ancestors, dept_name, order_num, status, del_flag, create_by, create_time)
SELECT 110, 100, '0,100', '读者', 3, '0', '0', 'admin', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dept WHERE dept_name = '读者' AND del_flag = '0');
-- 已经注册、还没有部门的读者，移进「读者」部门
UPDATE sys_user u
JOIN sys_user_role ur ON ur.user_id = u.user_id
JOIN sys_role r ON r.role_id = ur.role_id AND r.role_key = 'reader'
SET u.dept_id = (SELECT dept_id FROM (SELECT dept_id FROM sys_dept WHERE dept_name = '读者' AND del_flag = '0' LIMIT 1) t)
WHERE u.dept_id IS NULL AND u.del_flag = '0';

-- 3. 用户昵称
UPDATE sys_user SET nick_name = '管理员',   email = '' WHERE user_name = 'admin' AND nick_name = '若依';
UPDATE sys_user SET nick_name = '演示用户', email = '' WHERE user_name = 'ry'    AND nick_name = '若依';

-- 4. 删掉若依的 3 条公告（连同已读记录），换成一条自己的公告
DELETE FROM sys_notice_read WHERE notice_id IN (1, 2, 3);
DELETE FROM sys_notice WHERE notice_id IN (1, 2, 3) AND notice_title LIKE '%若依%';
INSERT INTO sys_notice (notice_title, notice_type, notice_content, status, create_by, create_time, remark)
SELECT '欢迎使用图书管理系统', '2',
       '<p>图书管理系统已上线。读者注册账号后，可以在「图书信息」里借书，在「我的借阅」里查看和归还。</p>',
       '0', 'admin', NOW(), '管理员'
WHERE NOT EXISTS (SELECT 1 FROM sys_notice WHERE notice_title = '欢迎使用图书管理系统');

COMMIT;
