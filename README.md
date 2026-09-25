# AgentGo Backend

AgentGo 的后端服务，采用 Kotlin 与 Gradle 多模块构建。

## 开发流程

代码变更遵循：`main → release/* → feature/* 或 fix/* → PR → release/* → PR → main`。

- `main` 和 `release/*` 仅接受 Pull Request 合并。
- 每次提交必须符合 `<emoji><type>: <message>` 格式，且单次提交变更少于 300 行。
- Pull Request 必须通过 Merge CI、测试和 85% 以上的代码覆盖率检查。

## 本地构建

安装 JDK 21 后执行：

```bash
./gradlew build
```

Windows PowerShell：

```powershell
.\gradlew.bat build
```

## 模块

- `agentgo-core`：领域模型与核心服务接口。
- `agentgo-app`：应用启动模块。
