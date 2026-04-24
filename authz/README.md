# authz Win11 部署目录

## 目录结构

- `app/`：后端可运行 jar
- `config/`：外置配置（`application-prod.yml`）
- `logs/`：启动输出日志（`app.out.log` / `app.err.log`）与 `app.pid`
- `bin/`：启动/停止脚本

## 1) 修改配置

编辑 `config/application-prod.yml`：

- `spring.datasource.url/username/password`
- `app.security.jwt.secret`（生产请替换为强随机字符串）
- `server.port`（如需）
- `app.security.cors.allowed-origins`（改成你的前端域名/端口）

## 2) 启动

双击：`bin/start.bat`

或命令行（推荐在 `cmd.exe`）：

```bat
cd /d D:\path\to\authz
bin\start.bat
```

若你在 PowerShell 里运行，建议用：

```powershell
cmd /c .\\bin\\start.bat
```

启动后会生成：

- `logs/app.pid`
- `logs/app.out.log`
- `logs/app.err.log`

## 3) 停止

双击：`bin/stop.bat`

或命令行：

```bat
cd /d D:\path\to\authz
bin\stop.bat
```

PowerShell 下建议：

```powershell
cmd /c .\\bin\\stop.bat
```

## 4) 最小验证清单

- 进程是否存在（拿 `logs/app.pid` 里的 pid 验证）：
  - `tasklist /fi "pid eq <pid>"`
- 端口是否监听：
  - `netstat -ano | findstr :8080`（或你配置的端口）
- 日志是否有启动信息/报错：
  - 查看 `logs/app.out.log` / `logs/app.err.log`
- 接口连通性：
  - 若有 `GET /api/ping`，访问 `http://127.0.0.1:8080/api/ping`

## 5) 前端（同级 `authz-web`）

与 `authz` 并列的 [`../authz-web`](../authz-web) 使用 **vite preview** 部署，默认 `http://127.0.0.1:4173`；详见该目录下 `README.md`。`config/application-prod.yml` 已包含 `localhost:4173` / `127.0.0.1:4173` 的 CORS，部署后若改端口请同步修改。

