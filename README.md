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

## 扩展说明

1. **新增业务模块**：按 `business/wxuser` 结构新建，遵循 Controller → Service → Mapper 分层
2. **获取当前用户**：`ContextCache.getUserId()` 或 `ContextCache.getUser()`
3. **业务异常**：`throw new BizException(code, "msg")`
