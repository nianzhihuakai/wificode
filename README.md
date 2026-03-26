# 小程序后端基础架构脚手架

基于已上线的 `wxg` 项目抽取的可复用架构，作为后续所有小程序后端项目的统一脚手架。

## 技术栈

| 类型 | 技术 |
|------|------|
| 框架 | Spring Boot 4.0.1 |
| 语言 | Java 21 |
| ORM | MyBatis-Plus 3.5.16 |
| 数据库 | PostgreSQL |
| 认证 | JWT (com.auth0/java-jwt) |

## 项目结构

```
src/main/java/com/nzhk/wificode/
├── MiniappApplication.java          # 启动类
├── common/                          # 公共模块
│   ├── config/                      # WebMvcConfig, RestTemplateConfig, JwtConfig
│   ├── cache/                       # ContextCache, UserInfo（用户上下文）
│   ├── exception/                   # BizException, GlobalExceptionHandler
│   ├── info/                        # RequestInfo, ResponseInfo（统一封装）
│   ├── interceptor/                 # JwtInterceptor
│   └── utils/                       # JwtUtil, ResponseUtil, IdUtil, BeanConvertUtil
├── business/wxuser/                 # 用户业务
│   ├── controller/WxUserController
│   ├── service/IWxUserService, WxUserServiceImpl
│   ├── entity/WxUser
│   ├── bean/                        # 请求/响应 DTO
│   └── vo/WxLoginResVO
├── wechat/config/                   # 微信配置 WechatProperties
└── mapper/                          # MyBatis Mapper
```

## 可复用功能

### 1. 登录认证

- **登录接口**：`POST /wificode/user/login`
- 请求体：`{ "data": { "code": "微信js_code", "userId": "可选" } }`
- 返回：`{ "code": 200, "success": true, "data": { "token": "...", "userInfo": {...} } }`

### 2. 用户信息

- **保存用户信息**：`POST /wificode/user/saveUserInfo`（需 token）
- **获取用户信息**：`POST /wificode/user/getUserInfo`（需 token）

### 3. 认证方式

- 请求头携带：`token: <JWT>`
- 登录、文件访问等路径已放行

### 4. 统一响应格式

```json
{
  "code": 200,
  "msg": "success",
  "success": true,
  "data": { ... }
}
```

## 配置说明

在 `application-dev.yml` 或环境变量中配置：

```yaml
wificode:
  jwt:
    secret: ${MINIAPP_JWT_SECRET}   # JWT 密钥
  wechat:
    appid: ${MINIAPP_WECHAT_APPID}  # 小程序 appid
    secret: ${MINIAPP_WECHAT_SECRET} # 小程序 secret
```

## 数据库初始化

执行 `docs/wx_user_table.sql` 创建用户表。

```bash
psql -U postgres -d miniapp_dev -f docs/wx_user_table.sql
```

## 启动

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 扫码统计与店主绑定（WiFi 码）

- **语义**：`wifi_code.user_id` 为推销员（创建者）；`store_owner_id` 为店主，通过绑定码核销后写入。
- **已有库升级**：依次执行 [`docs/migration_v2_scan_bind.sql`](docs/migration_v2_scan_bind.sql)、[`docs/migration_v3_user_roles.sql`](docs/migration_v3_user_roles.sql)、[`docs/migration_v4_bind_ticket_one_per_wifi.sql`](docs/migration_v4_bind_ticket_one_per_wifi.sql)（绑定票据一码一行）；新环境可直接用 [`docs/create.sql`](docs/create.sql)。
- **用户角色**：`wx_user.roles` 为 PostgreSQL `VARCHAR[]`，默认 `{SALES}`；管理员可在库中写入 `ADMIN`，例如：  
  `UPDATE wx_user SET roles = ARRAY['SALES','ADMIN']::varchar[] WHERE id = '用户id';`
- **登录/用户信息**：返回 `roles`（数组）、`admin`、`hasStoreBind`（是否已绑定门店），供小程序控制菜单。
- **配置（限流，可调）**：`wificode.stats` 下 `report-rate-limit-per-minute`、`bind-attempts-per-day`（绑定尝试/日/用户）、`bind-code-per-user-per-minute`、`bind-code-per-wifi-per-hour`、`wifi-code-write-per-user-per-minute`（新建+编辑合计/分钟/用户）。

### 接口（均带 context-path `/wificode`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/public/reportConnection` | 有效连接上报，**无需登录**；`data.wifiCodeId` 必填；可选请求头 `token`（已登录则按用户+自然日去重） |
| GET | `/list` | 推销员：本人创建的码列表（需 token） |
| GET | `/list/store` | 店主：已绑定为本人的码列表（需 token） |
| POST | `/bindCode` | 推销员生成店主绑定码，`data.wifiCodeId`（需 token） |
| POST | `/store/bind` | 店主核销绑定码，`data.bindCode`（需 token） |

`POST /public/reportConnection` 请求体示例：

```json
{ "data": { "wifiCodeId": "xxx", "deviceInfo": "可选" } }
```

响应 `data`：`{ "counted": true, "totalCount": 123 }`（`counted` 表示本次是否新计 1 次）。

### 小程序侧约定（契约）

1. 扫码进入页：`onLaunch` / `onLoad` 解析 `scene` 或 `query` 中的 `id`（WiFi 码主键），与现有 `public/get?id=` 一致。
2. 在确认进入承载页（如 `pages/wifiqrcode/wifiqrcode`）后调用 `POST /wificode/public/reportConnection`；若用户已登录，带上 `token` 以便按「同一用户+同一码+同一天」只计一次。
3. 未登录访客依赖 IP 去重；若无法取得 IP（如 `unknown`），接口会返回业务错误码 `40005`，可引导登录后再上报。
4. 店主在「我的」等入口输入推销员提供的绑定码，调用 `POST /wificode/store/bind`（需登录）。

### 定时任务

每日 `00:05`（服务器时区）根据 `wifi_scan_log.stat_date` 回写各码 `yesterday_count`。

### 远期广告分润（占位）

表 `revenue_share_rule`、`ad_revenue_ledger` 已在 `docs/create.sql` 与迁移脚本中创建，对接微信流量主结算与打款流程后再写入业务数据。

## 扩展说明

1. **新增业务模块**：按 `business/wxuser` 结构新建，遵循 Controller → Service → Mapper 分层
2. **获取当前用户**：`ContextCache.getUserId()` 或 `ContextCache.getUser()`
3. **业务异常**：`throw new BizException(code, "msg")`
