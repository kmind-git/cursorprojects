# 登录与权限管理系统（Spring Boot + Vue）

## MVP 决策（已固化）

- **权限粒度**：统一使用 `permission_code`（同时用于后端 API 授权与前端按钮/功能点控制）
- **菜单**：支持**动态菜单**（后端维护菜单树并按权限下发；前端据此生成路由/侧边栏）
- **权限下发策略**：`POST /api/auth/login` 与 `GET /api/auth/me` 返回用户信息 + 权限 codes + 菜单树
- **Refresh Token 存储**：**httpOnly Cookie**（更安全，避免前端可读）  
  - Refresh 请求使用 **Double-Submit CSRF**：服务端发 `csrf_token`（非 httpOnly）Cookie，前端在 `X-CSRF-Token` 头回传并与 Cookie 比对

## 目录结构

- `backend/`：Spring Boot 3（JWT + RBAC + 审计）
- `frontend/`：Vue 3 + Vite + Pinia + Vue Router（登录、动态路由、按钮权限）

## 验收清单（MVP）

- 登录/退出/自动续期可用；无 token 返回 401、无权限返回 403
- 后端接口可用 `@PreAuthorize("hasAuthority('xxx')")` 做权限拦截
- 前端路由/菜单按权限动态生成；按钮按权限显示/禁用
- 可在管理端完成 用户/角色/权限/菜单 的 CRUD 与授权分配
- 关键行为具备审计记录（登录、创建/修改/删除、分配权限等）

