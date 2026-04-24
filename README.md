# 登录与权限管理系统（Spring Boot + Vue）

## 项目现状（2026-04）

- **前端（新登录页）**：`dev-front/`（Vue 3 + Vite + Tailwind，新登录页 UI + 调用后端登录接口）
- **其它**：`fixtest/`（独立的 FIX 客户端示例项目，详见其目录 README）

> 说明：当前仓库已不包含后端工程（原 `backend/` 已删除）。`dev-front` 仍按 `/api/*` 代理方式对接外部后端服务。

## MVP 决策（已固化）

- **权限粒度**：统一使用 `permission_code`（同时用于后端 API 授权与前端按钮/功能点控制）
- **菜单**：支持**动态菜单**（后端维护菜单树并按权限下发；前端据此生成路由/侧边栏）
- **权限下发策略**：`POST /api/auth/login` 与 `GET /api/auth/me` 返回用户信息 + 权限 codes + 菜单树
- **Refresh Token 存储**：**httpOnly Cookie**（更安全，避免前端可读）  
  - Refresh 请求使用 **Double-Submit CSRF**：服务端发 `csrf_token`（非 httpOnly）Cookie，前端在 `X-CSRF-Token` 头回传并与 Cookie 比对

## 目录结构

- `dev-front/`：Vue 3 + Vite + Tailwind（新登录页）
- `fixtest/`：FIX 4.4 客户端 / QuickFIX Python

## 验收清单（MVP）

- 登录/退出/自动续期可用；无 token 返回 401、无权限返回 403
- 后端接口可用 `@PreAuthorize("hasAuthority('xxx')")` 做权限拦截
- 前端路由/菜单按权限动态生成；按钮按权限显示/禁用
- 可在管理端完成 用户/角色/权限/菜单 的 CRUD 与授权分配
- 关键行为具备审计记录（登录、创建/修改/删除、分配权限等）

## 本地运行（开发）

### 运行新登录页 `dev-front/`（端口 5173）

- **端口**：`5173`
- **API 代理**：前端请求 `/api/*` 会被代理到后端（默认 `http://localhost:8080`）
  - 可通过环境变量 `API_TARGET` 覆盖

```bash
cd dev-front
npm install
npm run dev
```

浏览器访问：`http://localhost:5173/`

> 注意：`API_TARGET` 指向你的后端服务地址（例如 `http://localhost:8080`）。本仓库不再提供后端启动方式。

## 认证接口速览（后端）

- `POST /api/auth/login`：登录（返回 `accessToken` + `permissions` + `menus`，并下发 `refresh_token` / `csrf_token` Cookie）
- `POST /api/auth/refresh`：刷新 access token（需要 `X-CSRF-Token` 与 `csrf_token` Cookie 双提交校验）
- `POST /api/auth/logout`：登出（需要 CSRF 校验）
- `GET /api/auth/me`：获取当前用户信息（需要 Bearer JWT）

## 日志与忽略规则

运行期日志（例如 `authz/logs/` 下文件）属于生成物，已在仓库根 `.gitignore` 中忽略。

