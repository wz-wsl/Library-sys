# 图书管理系统

基于 [若依 RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) v3.9.2（前后端分离版）二次开发的图书管理系统。

若依提供了用户、角色、菜单、部门、权限、日志等后台基础功能；在此之上，本项目手写实现了图书信息、图书分类、借书还书和读者注册等业务模块。

## 功能

### 管理员

- **图书信息**：新增、修改、删除、查询，可按书名、作者、分类（含子分类）、状态搜索
  - 馆藏总数和可借数量联动：修改馆藏总数时，不能少于已借出的数量
  - 还有未归还的书不能删除；有过借阅记录的书也不能删除，改为下架以保留历史
- **图书分类**：树形分类，支持新增、删除、移动
  - 下面还有子分类或图书的分类不能删除
  - 不能把分类移到它自己或它的子分类下面
- **系统管理**：用户、角色、菜单、部门、参数、公告、日志等（若依自带）

### 读者

- **自助注册**：注册后自动获得「读者」角色，并放进「读者」部门
- **借书**：在图书信息页借阅上架且有库存的图书，借期 14 天；同一本书归还之前不能重复借
- **我的借阅**：只能看到自己的借阅记录，可以在这里还书；超过应还时间的记录显示「已逾期」
- **首页**：欢迎语、馆藏图书数量、我的在借和已逾期数量、在借图书列表

## 技术栈

| | 技术 |
|---|---|
| 后端 | Spring Boot 3.5、Java 17+、Spring Security + JWT、MyBatis + PageHelper |
| 前端 | Vue 3、Vite、Element Plus、Pinia |
| 存储 | MySQL 8、Redis |

## 目录结构

```
RuoYi-Vue/                                  后端（Maven 多模块）
├── ruoyi-admin/.../web/controller/library/     图书模块的 Controller
├── ruoyi-system/.../com/ruoyi/library/         图书模块的实体、Mapper、Service
├── ruoyi-system/src/main/resources/mapper/library/   Mapper XML
└── sql/                                         数据库脚本
RuoYi-Vue3-3.9.2/                           前端
├── src/api/library/                            图书模块的接口
└── src/views/library/                          页面：book 图书信息、category 图书分类、borrow 我的借阅
```

## 数据库

图书模块有 3 张表，都以 `lib_` 开头：

| 表 | 说明 |
|---|---|
| `lib_category` | 图书分类，`parent_id` 组成树形结构 |
| `lib_book` | 图书信息，`total_count` 馆藏总数、`stock_count` 可借数量、`status` 0 上架 / 1 下架 |
| `lib_borrow` | 借阅记录，`due_time` 应还时间、`return_time` 归还时间、`status` 0 借阅中 / 1 已归还 / 2 已逾期 |

`RuoYi-Vue/sql/` 下的脚本：

| 脚本 | 作用 |
|---|---|
| `ry_20260417.sql`、`quartz.sql` | 若依自带的系统表和定时任务表 |
| `library.sql` | 创建 3 张图书表（**会先删除同名表**，表里已有数据时不要执行） |
| `library_menu.sql` | 「图书管理」目录，「图书信息」「图书分类」菜单及其按钮权限 |
| `library_borrow_menu.sql` | 「我的借阅」菜单以及借书、还书按钮权限 |
| `library_reader_role.sql` | 创建「读者」角色并分配菜单 |
| `library_branding.sql` | 把若依的演示数据换成本系统的部门、昵称、公告，创建「读者」部门 |

## 本地运行

**1. 准备 MySQL 和 Redis**

创建数据库 `ry-vue`（字符集 utf8mb4），然后按顺序执行：

1. `ry_20260417.sql`、`quartz.sql`
2. `library.sql`
3. `library_menu.sql`、`library_borrow_menu.sql`
4. `library_reader_role.sql`、`library_branding.sql`

除了 `library.sql` 以外，`library_*.sql` 脚本都可以重复执行。

Redis 使用默认的 `localhost:6379`，无密码。

**2. 启动后端**

数据库连接在 `ruoyi-admin/src/main/resources/application-druid.yml` 中配置，上传目录在 `application.yml` 的 `ruoyi.profile` 中配置。

```bash
cd RuoYi-Vue
mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

后端端口为 8080，也可以在 IDE 里直接运行 `com.ruoyi.RuoYiApplication`。

**3. 启动前端**

```bash
cd RuoYi-Vue3-3.9.2
npm install --registry=https://registry.npmmirror.com
npm run dev
```

浏览器访问 <http://localhost>，默认管理员账号为 `admin / admin123`。

**4. 开启读者注册**

若依默认关闭注册。在「系统管理 → 参数设置」里，把「账号自助-是否开启用户注册功能」改成 `true`，登录页就会出现「立即注册」。

## 致谢与许可

本项目基于以下开源项目开发，感谢若依作者的工作：

- 后端：[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)
- 前端：[RuoYi-Vue3](https://github.com/yangzongzhuan/RuoYi-Vue3)

若依框架采用 MIT 协议，原版权声明保留在 `RuoYi-Vue/LICENSE` 和 `RuoYi-Vue3-3.9.2/LICENSE` 中。
