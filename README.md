<p align="center">
  <img src="agentgo-app/src/main/resources/agentgo-logo.png" alt="AgentGo Logo" width="180">
</p>

<h1 align="center">AgentGo Backend</h1>

<p align="center">
  <a href="https://github.com/Martin-WMM/AgentGo-backend/actions/workflows/merge-ci.yml"><img src="https://github.com/Martin-WMM/AgentGo-backend/actions/workflows/merge-ci.yml/badge.svg?branch=main" alt="Merge CI"></a>
  <a href="https://img.shields.io/github/stars/Martin-WMM/AgentGo-backend"><img src="https://img.shields.io/github/stars/Martin-WMM/AgentGo-backend" alt="GitHub stars"></a>
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin 2.3.0">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.1.1">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Gradle-9.1%2B-02303A?logo=gradle&logoColor=white" alt="Gradle 9.1+">
</p>

## 1. Introduction / 简介

AgentGo Backend is the Kotlin, Spring Boot, and Spring MVC service that provides
authentication, agent workflows, persistence, file storage, and integration APIs.

AgentGo Backend 是基于 Kotlin、Spring Boot 和 Spring MVC 的服务端，提供认证、Agent 工作流、持久化、文件存储及集成 API。

## 2. Updates / 更新

- Modular Gradle architecture with reusable application starters.
- Authentik integration, profile/session APIs, and file capabilities.
- Coverage and build quality gates are enforced by CI.

- 采用模块化 Gradle 架构和可复用应用 Starter。
- 已接入 Authentik，并提供用户资料、会话和文件能力。
- CI 强制执行测试、覆盖率和构建质量检查。

## 3. Getting Started / 快速开始

Requirements / 环境要求: JDK 25, Gradle 9.1+, and Docker。

```bash
gradle test koverVerify build

cd local-deployments
cp .env.example .env
docker compose up --build -d
```

The service is available at `http://localhost:8080`; health and OpenAPI endpoints
are `/actuator/health` and `/swagger-ui.html`.

服务地址为 `http://localhost:8080`，健康检查和 OpenAPI 地址分别为
`/actuator/health` 和 `/swagger-ui.html`。

## 4. Contribution / 参与贡献

Read [AGENTS.md](AGENTS.md), follow the protected branch flow, and keep API changes
documented in [AgentGo Docs](https://github.com/Martin-WMM/AgentGo-docs)。

请先阅读 [AGENTS.md](AGENTS.md)，遵守受保护分支流程，并在
[AgentGo Docs](https://github.com/Martin-WMM/AgentGo-docs) 中同步 API 变更。

## 5. License / 许可证

This project is governed by the [AgentGo Proprietary License](LICENSE)。All rights
belong to Martin M. W. (王美民). Any use, modification, distribution, or commercial
use requires prior written confirmation at `blessedwmm@gmail.com`。

本项目采用 [AgentGo Proprietary License](LICENSE)。所有权利归 Martin M. W.（王美民）所有。
任何使用、修改、分发或商业用途，均须先通过 `blessedwmm@gmail.com` 获得本人书面确认授权。

## Related Projects / 相关项目

- [AgentGo UI](https://github.com/Martin-WMM/AgentGo-UI) · [AgentGo Desktop](https://github.com/Martin-WMM/AgentGo-desktop)
- [AgentGo Docs](https://github.com/Martin-WMM/AgentGo-docs)
