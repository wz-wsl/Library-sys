# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库布局

本目录包含若依 RuoYi v3.9.2 前后端分离项目的两个独立工程，开发时需要同时运行：

- `RuoYi-Vue/` —— 后端，Spring Boot 3.5 + Java 17 的 Maven 多模块工程（是一个 git 仓库，springboot3 分支）。
- `RuoYi-Vue3-3.9.2/` —— 前端，Vue 3 + Vite + Element Plus + Pinia（不是 git 仓库）。

两个工程都没有测试代码（任何模块下都没有 `src/test`），也没有 lint/格式化配置。修改后的验证方式是编译/构建，再在运行中的应用里实际操作。

## 常用命令

### 后端（在 `RuoYi-Vue/` 下执行）

本机 `mvn` 不在 PATH 中，使用 IDEA 自带的 Maven：`"D:\IntelliJ IDEA 2026.2.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd"`。

```bash
mvn clean package -Dmaven.test.skip=true   # 打包，产物为 ruoyi-admin/target/ruoyi-admin.jar（同 bin/package.bat）
mvn -pl ruoyi-system -am compile           # 只编译某个模块及其依赖
java -jar ruoyi-admin/target/ruoyi-admin.jar  # 启动，端口 8080（或在 IDE 里运行 com.ruoyi.RuoYiApplication）
```

依赖从阿里云镜像下载；如果报 `Remote host terminated the handshake`，加 `-U` 重试即可。

### 前端（在 `RuoYi-Vue3-3.9.2/` 下执行）

```bash
npm install --registry=https://registry.npmmirror.com   # README 和 bin/*.bat 用的是 yarn，npm 也可以
npm run dev          # 端口 80，把 /dev-api/* 代理到 http://localhost:8080 并去掉前缀
npm run build:prod   # 输出到 dist/；build:stage 使用 .env.staging
```

npm 11 会跳过 `esbuild` 等包的 install 脚本，但已验证 esbuild 和 `vite build` 仍能正常工作。

### 运行时依赖

- **MySQL**：数据库名 `ry-vue`，由 `RuoYi-Vue/sql/ry_20260417.sql` 和 `RuoYi-Vue/sql/quartz.sql` 初始化。连接信息在 `ruoyi-admin/src/main/resources/application-druid.yml`。
- **Redis**：`localhost:6379`，配置在 `application.yml`。登录离不开 Redis，验证码和登录会话都存在里面。
- **默认账号**：`admin/admin123`。
- **上传目录**：`ruoyi.profile`（`D:/ruoyi/uploadPath`），对外通过 `/profile/**` 访问。

## 后端架构

模块依赖关系：`ruoyi-admin` → `ruoyi-framework` → `ruoyi-system` → `ruoyi-common`；`ruoyi-quartz` 和 `ruoyi-generator` 只依赖 `ruoyi-common`，并由 `ruoyi-admin` 引入。

- `ruoyi-admin`：所有 Controller（`web/controller/{system,monitor,common,tool}`）以及全部配置文件。
- `ruoyi-framework`：安全、切面、拦截器、数据源、全局异常处理。
- `ruoyi-system`：业务的 service/mapper/domain 和 Mapper XML。
- `ruoyi-common`：注解、工具类、基础类型。

核心实体 `SysUser`/`SysRole`/`SysMenu`/`SysDept`/`SysDictData`/`SysDictType` 放在 `ruoyi-common/core/domain/entity` 而不是 `ruoyi-system`，因为 framework 层也需要用到它们。

### 认证与授权

- **会话存储**：JWT 本身只携带一个 uuid，完整的 `LoginUser` 存在 Redis 的 `login_tokens:<uuid>` 下，有效期为 `token.expireTime`（30 分钟）。
- **续期与失效**：`JwtAuthenticationTokenFilter` 在每个请求上恢复 SecurityContext；`TokenService.verifyToken` 在剩余不足 20 分钟时续期。让会话失效就是删掉这个 Redis key（强退、注销都是这么做的）。
- **角色权限变更**：会通过 `TokenService.refreshPermissionByRoleId` 刷新在线用户的权限缓存。
- **接口鉴权**：使用 `@PreAuthorize("@ss.hasPermi('system:user:list')")`。`ss` 是 `PermissionService`，其他方法还有 `hasAnyPermi`、`hasRole`、`hasAnyRoles`。
- **权限字符串来源**：存在 `sys_menu.perms`；超级管理员（userId 1）拥有 `*:*:*`。前端用的是同一套字符串。
- **匿名访问**：在 Controller 方法或类上加 `@Anonymous`（由 `PermitAllUrlProperties` 收集），或者加到 `SecurityConfig` 里。

### 请求与响应约定

- **Controller 写法**：继承 `BaseController`。
  - 列表接口：先 `startPage()`，再执行查询，最后 `getDataTable(list)`，返回 `TableDataInfo`（`total`、`rows`）。PageHelper 从请求参数读取 `pageNum`/`pageSize`/`orderByColumn`/`isAsc`，而且只对 `startPage()` 之后的**第一条**查询生效。
  - 其他接口：返回 `AjaxResult`（`code`/`msg`/`data`，可用 `put` 附加额外字段），或用 `success()`/`error()`/`toAjax(rows)`。
- **业务错误**：抛 `ServiceException`，由 `GlobalExceptionHandler` 处理。
- **前端识别的状态码**：200、401（弹出重新登录）、500（错误提示）、601（`HttpStatus.WARN`，警告提示）。
- **XSS 过滤**：对 `/system/*`、`/monitor/*`、`/tool/*` 生效，`/system/notice` 除外（公告需要保存富文本 HTML）。

### 横切注解

注解定义在 `ruoyi-common/annotation`，对应的切面在 `ruoyi-framework/aspectj`：

- `@Log(title, businessType)`：通过 `AsyncManager` 异步写入 `sys_oper_log`。
- `@DataScope(deptAlias, userAlias)`：根据当前用户各角色的数据范围生成一段 SQL，写入第一个参数（必须是 `BaseEntity`）的 `params.dataScope`。对应的 Mapper XML 必须引用 `${params.dataScope}`，参考 `SysUserMapper.xml`、`SysRoleMapper.xml`、`SysDeptMapper.xml`。
- `@DataSource(DataSourceType.SLAVE)`：切换数据源。从库默认关闭，在 `application-druid.yml` 里开启。
- 其他：`@RateLimiter`（基于 Redis）、`@RepeatSubmit`（拦截器）、`@Excel`/`@Excels`（配合 `ExcelUtil` 导入导出）、`@Sensitive`（JSON 脱敏）。

### MyBatis

- **XML 位置**：`<module>/src/main/resources/mapper/**/*Mapper.xml`。
- **字段映射**：`mapUnderscoreToCamelCase` 是关闭的，每个 Mapper 都用显式的 `resultMap`。新增字段时，要同时改 `resultMap`、select 列表，以及 insert/update 里的 `<if>` 块。
- **类型别名**：别名包为 `com.ruoyi.**.domain`。
- **实体基类**：实体继承 `BaseEntity`（包含 createBy/createTime/updateBy/updateTime/remark/params）；树形实体继承 `TreeEntity`。

### 定时任务（ruoyi-quartz）

- **任务定义**：任务存在 `sys_job` 表中。`invokeTarget` 是一个调用字符串，例如 `ryTask.ryParams('ry')`，用 bean 名或全限定类名指定目标。
- **安全限制**：调用目标必须位于 `Constants.JOB_WHITELIST_STR`（`com.ruoyi.quartz.task`）中，且不能命中 `JOB_ERROR_STR` 黑名单。新的任务类要放在 `com.ruoyi.quartz.task` 包下。

### 代码生成器（ruoyi-generator）

- **配置**：`generator.yml`，默认 `packageName: com.ruoyi.system`、`tablePrefix: sys_`。
- **模板**：在 `resources/vm/`。`tplWebType` 为 `element-plus` 时使用 `vm/vue/v3`，与本前端匹配；`element-plus-typescript` 对应 `vm/vue/v3ts`。
- **生成内容**：Controller/Service/Mapper/XML、`api/*.js`、`views/*.vue`，以及一段需要导入 `sys_menu` 的菜单 SQL。

### 系统参数与字典

- 运行时参数存在 `sys_config` 表，字典存在 `sys_dict_type`/`sys_dict_data` 表，两者都缓存在 Redis 中。
- 后端的 i18n 消息在 `ruoyi-admin/src/main/resources/i18n/messages.properties`，通过 `MessageUtils.message(key)` 获取。

## 前端架构

### 菜单驱动的动态路由

- **路由守卫**：`src/permission.js` 在拿到 token 后调用 `getInfo`（角色、权限）和 `getRouters`（菜单树）。
- **组件解析**：`store/modules/permission.js` 中的 `loadView` 用 `import.meta.glob` 把每个菜单的 `component` 字符串映射到 `src/views/<component>.vue`；`Layout`、`ParentView`、`InnerLink` 是特殊的组件名。
- **新增页面**：创建 view 文件，再在 `sys_menu`（菜单管理）里添加一条 component 路径（相对于 `views/`，不带 `.vue`）。不需要改前端路由表。
- **不在菜单里的页面**：例如 `/system/user-auth/role/:userId`，放在 `src/router/index.js` 的 `dynamicRoutes` 中，用 `permissions`/`roles` 控制访问。

### 权限控制

按钮级别的权限用 `v-hasPermi="['system:user:add']"` / `v-hasRole`，脚本里用 `this.$auth` / `auth.hasPermi()`，与后端使用相同的 `perms` 字符串。

### HTTP 请求

`src/utils/request.js` 中：

- `baseURL` 取 `VITE_APP_BASE_API`（开发环境 `/dev-api`，生产环境 `/prod-api`），每个请求自动附加 `Authorization: Bearer <token>`。
- GET 的 params 会被序列化到 URL 上。
- POST/PUT 有客户端防重复提交：1 秒内相同 url+data 会被拒绝。可以用请求头 `repeatSubmit: false` 关闭，用 `isToken: false` 跳过 token。
- `download(url, params, filename)` 以表单编码的方式 POST，并把返回的 blob 保存为文件。

`src/api/<area>/<x>.js` 与后端 Controller 的路径一一对应。

### 全局注册

- **全局组件**（`main.js`）：`DictTag`、`Pagination`、`RightToolbar`、`FileUpload`、`ImageUpload`、`ImagePreview`、`Editor`、`svg-icon`。
- **全局属性**：`useDict`、`parseTime`、`resetForm`、`handleTree`、`addDateRange`、`selectDictLabel`、`download`，以及插件 `$tab`、`$modal`、`$auth`、`$cache`、`$download`。
- **自动导入**：`unplugin-auto-import` 会自动导入 Vue / Vue Router / Pinia 的 API，所以 `ref`、`defineStore` 等不需要写 import。

### 字典与状态

- **字典用法**：`const { sys_normal_disable } = useDict('sys_normal_disable')`，再配合 `<dict-tag :options="sys_normal_disable" :value="..."/>`；数据缓存在 `store/modules/dict.js`。
- **Pinia stores**（`src/store/modules`）：`user`（token、角色、权限）、`permission`（路由）、`dict`、`settings`、`tagsView`、`app`、`lock`。
