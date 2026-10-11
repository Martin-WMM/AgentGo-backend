# Changelog

All notable changes to AgentGo Backend are documented in this file.

## [0.1.0-SNAPSHOT] - Unreleased

### Fixed

- Workspace and avatar file upload/delete now use programmatic database transactions with
  MinIO compensation: upload commits metadata after object storage and rolls the object back
  when metadata persistence fails; delete removes metadata first and deletes the object only
  after a successful commit.
- Avatar replacement lookups now match keys under `Avatars/{owner-hash}` (including
  extension suffixes) instead of an incorrect trailing-slash prefix.
- Raised the Gradle Wrapper from 9.0.0 to 9.8.1 so the build can run on JDK 25. Gradle 9.0.0
  rejects that JVM because its maximum Gradle JVM is 24.
- RP-initiated logout now sends Authentik `id_token_hint` with `post_logout_redirect_uri`
  (Authentik rejects the redirect alone as a malformed request).
- Local Compose OIDC token/JWKS/userinfo calls go through `authentik-oidc` Host rewrite so
  ID token `iss` is `http://localhost:9000/...` and browser end-session validates the hint.
- Compose splits **dev** infra (default) from **local** UI/app (`--profile local`) so
  `:5173`/`:8080` and `:20080`/`:20082` stay isolated.

### Changed

- `GET /api/v1/promptTemplates` is now paginated. Query parameters are one-based `page`,
  `size` (default 20, maximum 100), repeatable `sort` (`name`, `engine`, `createdAt`,
  `updatedAt`), and `order` (`ASC` or `DESC`). Omitting `sort` keeps newest `updatedAt`
  first. The envelope `data` field is a `PageResponse`.

### Added

- Added `agentgo-app-mcp-servers` with user-scoped MCP Streamable HTTP server management
  (`/api/v1/mcpServers`), Flyway `V12__create_mcp_server` (`tbl_mcp_server`), and
  `agentgo-dto-mcp`. Local/stdio transports are rejected. CRUD plus `POST /{id}/probe`
  runs MCP `initialize` + `tools/list`, caches discovered tools, and updates connectivity
  status (`UNKNOWN` / `CONNECTED` / `DISCONNECTED` / `ERROR`). Success codes `9600`–`9605`;
  failures `MCP-001`…`MCP-006`.
- Per-user system settings are stored in `tbl_user_settings` (Flyway `V10__create_user_settings`)
  and exposed at `GET`/`PUT /api/v1/settings`. Theme, locale, sidebar, density, and reduced
  motion belong to the authenticated user. GET returns defaults with `persisted=false` until
  the first save.
- Authenticated user profiles are now stored in `tbl_user_profile` (Flyway `V8__create_user_profile`).
  Display name, email, username, and avatar URL stay synchronized with Authentik. Phone number,
  description, occupation, organization, homepage, location, time zone, and preferred language are
  stored in AgentGo so later agent runs can read personal context.
- File workspaces now store presentation `metadata`, including `metadata.color`, a palette accent
  (`#1D4ED8` by default). Create and update accept the color; omitting `metadata` on update keeps
  the stored accent. Flyway `V7__add_workspace_metadata` adds `tbl_workspace.metadata`.
- Added `agentgo-app-models` with user-scoped model CRUD (`/api/v1/models`), Flyway `tbl_model`,
  and the focused `agentgo-dto-models` DTO module for LLM, multimodal, embedding, ASR, TTS,
  Speech2Speech, and other provider configurations.
- Added shared `ApiVersions` / `ApiPaths` constants in `agentgo-commons` for URI path versioning.
- Expanded Springdoc OpenAPI tag, operation, parameter, response, and DTO schema descriptions
  for Authentication, Avatar, Files, and Models APIs.
- Added an authenticated user profile API and UI settings form that synchronize display name,
  email, and avatar URL with Authentik through the server-side Core User API.
- Added configurable SpringDoc OpenAPI metadata to `agentgo-springboot-starter`, including title,
  description, version, contact, license, terms, and optional server information.
- Added an isolated local Authentik identity-center deployment with PostgreSQL, secret-safe
  environment templates, and OIDC setup guidance.
- Branded the local Authentik default brand as AgentGo using the shared AgentGo Logo asset.
- Fixed the Authentik AgentGo brand asset mount to use the public media directory so the logo
  resolves in the admin interface.
- Added a `local-deployment` Docker Compose stack for PostgreSQL, Authentik, MinIO, and the
  executable web service.
- Added Docker build and ignore files, an environment example, and local deployment verification instructions.
- Added the `db-migration` Flyway migration directory and the initial schema metadata migration.
- Added the `agentgo-app-modules` group with composable web, observability, persistence, and AI Spring Boot starters.
- Added the `agentgo-app` Spring Boot web service module.
- Added the `agentgo-cli` Spring Shell command-line module with the `agentgo version` command.
- Added the `agentgo-commons` shared utility module.
- Added the `agentgo-core` AI workflow module with LangGraph4j integration.
- Added the `agentgo-dto` module for cross-module data transfer objects and protocol models.
- Added the `agentgo-springboot-starter` module with shared auto-configuration and foundational beans.
- Added Actuator, Prometheus metrics, OpenTelemetry tracing, Spring AI, Spring Data JPA, PostgreSQL, Springdoc OpenAPI, and Spring Shell integration points.
- Added GitHub Actions for commit review, dependency review, Merge CI, CodeQL, and tagged releases.
- Added repository issue and pull request templates, security policy documentation, and branch protection rules.

### Removed

- Removed the unused Spring AI OpenAI starter from the application classpath. Model calls are not wired yet.

### Changed

- Moved business HTTP APIs to `/api/v1/...` (`files`, `models`, avatar). Unversioned
  `/api/files/**` and `/api/models/**` are removed. Auth remains at `/api/auth/**`.
- Consolidated local Compose into `local-deployment/` and removed the duplicate
  `local-deployments/` directory.
- Raised the workspace and avatar upload limit to 100 MB and return FILE-011 through FILE-014
  in the standard `CommonHttpResponse` envelope when an upload is rejected.
- Centralized project, Java, Kotlin, Spring Boot, Springdoc, LangGraph4j, Spring Shell, and Kover versions in the root `build.gradle.kts`.
- Standardized the toolchain on Java 25 LTS, Kotlin 2.3.x, and Gradle 9.1+.
- Configured the build to publish exactly two executable Spring Boot Jars: the web application and the CLI.
- Kept commons, core, DTO, and starter modules as internal library modules rather than standalone release artifacts.

### Quality

- Required commit format: `<emoji><type>: <message>`.
- Enforced a changed-line limit of fewer than 300 lines per commit.
- Required tests and at least 85% code coverage in Merge CI.
- Enabled dependency review, secret scanning, security advisories, and code scanning workflows.
