## fixtest（FIX 4.4 客户端 / QuickFIX Python）

这是一个最小可运行的 FIX 4.4 **Initiator** 客户端项目，使用 **QuickFIX 的 Python bindings** 作为引擎，加载你的定制 `FIX44.xml`，连接并登录既有 FIX 服务端。

### 目录结构
- `config/fix_client.cfg`: QuickFIX 会话配置（你把连接参数填到这里）
- `config/FIX44.xml`: FIX4.4 数据字典（放你已有的定制版）
- `src/fixtest/app.py`: 入口，启动 initiator
- `src/fixtest/application.py`: QuickFIX Application 回调实现（含 Logon 注入自定义 tag）
- `src/fixtest/messages.py`: 示例消息构造/发送（TestRequest 等）
- `var/`: 运行期生成（store/log）

### 环境要求
- Windows 10/11 + Python 3.10+（建议）
- QuickFIX Python bindings（安装方式依赖你的 Python 版本与环境）

`requirements.txt` 里仅列出纯 Python 依赖；QuickFIX 本体通常需要你安装对应的 `quickfix` wheel/包（不同发行渠道命名可能不同）。

### 准备配置
1) 把你的定制字典放到：
- `config/FIX44.xml`

2) 编辑：
- `config/fix_client.cfg`

至少需要填：
- `SocketConnectHost`
- `SocketConnectPort`
- `SenderCompID`
- `TargetCompID`
- `HeartBtInt`

如 Logon 需要用户名/密码或其它自定义 tag：在 `config/fix_client.cfg` 的 `[DEFAULT]` 下填：
- `LogonTag.<tag>=<value>`

例如（仅示例）：
- `LogonTag.553=myuser`
- `LogonTag.554=mypassword`

### 运行
在 `fixtest/` 目录下：

推荐（可编辑安装）：

```bash
python -m pip install -e .
python -m fixtest --config config/fix_client.cfg
```

不安装也可以（临时设置模块搜索路径）：

```bash
set PYTHONPATH=src
python -m fixtest --config config/fix_client.cfg
```

或直接运行脚本：

```bash
python src/fixtest/app.py --config config/fix_client.cfg
```

### 排错提示（常见）
- **立即 Logout/Reject**：通常是 Logon 必填字段缺失、CompID 不匹配、EncryptMethod/HeartBtInt 不符合服务端要求，或字典校验失败。
- **序号问题（MsgSeqNum too low/high）**：检查 `var/store/` 是否有历史会话序号；开发阶段可临时设置 `ResetOnLogon=Y`（谨慎使用）。
- **字典问题**：确认 `DataDictionary` 指向的是你的定制 `FIX44.xml`，并且客户端/服务端的字典一致（自定义 tag/消息/required 字段）。

