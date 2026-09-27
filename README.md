<p align="center">
  <img src="https://raw.githubusercontent.com/Martin-WMM/AgentGo-UI/main/app/public/assets/logo-dark.png" alt="AgentGo" width="180">
</p>

<h1 align="center">AgentGo Backend</h1>

<p align="center">
  <a href="https://github.com/Martin-WMM/AgentGo-backend/actions/workflows/merge-ci.yml"><img src="https://github.com/Martin-WMM/AgentGo-backend/actions/workflows/merge-ci.yml/badge.svg?branch=main" alt="Merge CI"></a>
  <a href="https://github.com/Martin-WMM/AgentGo-backend"><img src="https://img.shields.io/github/stars/Martin-WMM/AgentGo-backend" alt="GitHub stars"></a>
</p>

Kotlin、Spring Boot 4 和 Spring MVC 构建的 AgentGo 服务端，负责 API、认证、Agent 工作流、持久化和文件存储。

## 快速开始

要求：JDK 25、Gradle 9.1+、Docker。

```bash
gradle test koverVerify build
```

启动本地服务：

```bash
cd local-deployments
cp .env.example .env
docker compose up --build -d
```

服务地址：`http://localhost:8080`；健康检查：`/actuator/health`；OpenAPI：`/swagger-ui.html`。

## 主要模块

`agentgo-app` 是可运行服务，`agentgo-cli` 是命令行客户端；其余模块提供通用模型、工作流、Spring Boot 基础设施、认证和文件能力。

## 文档

完整的架构、认证、部署、Terraform、API 和二次开发说明请查看 [AgentGo Docs](https://github.com/Martin-WMM/AgentGo-docs)。

- [快速上手](https://github.com/Martin-WMM/AgentGo-docs/tree/main/app/src/resources/%E5%BF%AB%E9%80%9F%E4%B8%8A%E6%89%8B)
- [集成与扩展](https://github.com/Martin-WMM/AgentGo-docs/tree/main/app/src/resources/%E9%9B%86%E6%88%90%E4%B8%8E%E6%89%A9%E5%B1%95)
- [二次开发](https://github.com/Martin-WMM/AgentGo-docs/tree/main/app/src/resources/%E4%BA%8C%E6%AC%A1%E5%BC%80%E5%8F%91)

## 参与贡献

请先阅读 [AGENTS.md](AGENTS.md) 和 [AgentGo Docs 贡献指南](https://github.com/Martin-WMM/AgentGo-docs/blob/main/CONTRIBUTING.md)。
