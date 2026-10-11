# AgentGo Backend Contribution Guide

This file defines the repository-specific instructions for working on AgentGo Backend.

## Project overview

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 multi-module project.
The project uses Java 25 LTS, Kotlin 2.3.x, and Gradle 9.1+.

All project and dependency versions are managed in the root `build.gradle.kts`. Do not add
module-local version literals when a version is already defined there.

## Module responsibilities

- `agentgo-app`: Executable Spring Boot web service. It owns HTTP endpoints, Actuator,
  Springdoc OpenAPI, persistence integration, metrics, tracing, and application wiring.
- `agentgo-app-modules`: Group of composable application capability starters consumed by
  `agentgo-app`.
- `agentgo-app-modules:agentgo-web-starter`: Web MVC and Springdoc OpenAPI capabilities.
- `agentgo-app-modules:agentgo-observability-starter`: Actuator, metrics, and tracing capabilities.
- `agentgo-app-modules:agentgo-persistence-starter`: JPA and PostgreSQL capabilities.
- `agentgo-app-modules:agentgo-ai-starter`: AgentGo core workflow capabilities. Spring AI is not wired in yet.
- `agentgo-app-modules:agentgo-app-auth`: Authentik OIDC login, session, and profile capabilities.
- `agentgo-app-modules:agentgo-app-file`: Workspace and avatar file metadata with MinIO storage.
- `agentgo-app-modules:agentgo-app-models`: Authenticated user-owned AI model configuration CRUD.
- `agentgo-app-modules:agentgo-app-mcp-servers`: Authenticated user-owned MCP Streamable HTTP server CRUD and probe.
- `agentgo-app-modules:agentgo-app-settings`: Per-user system settings that configure AgentGo clients.
- `agentgo-cli`: Executable Spring Shell command-line application for the `agentgo ...`
  command family.
- `agentgo-commons`: Shared utility types, helpers, and the standardized HTTP, pagination, and SSE response DTOs.
- `agentgo-core`: AI workflow and orchestration components, including LangGraph4j integration.
- `agentgo-dto`: Cross-module business data transfer objects and protocol models. DTOs shared
  between modules belong here, except for the standardized HTTP, pagination, and SSE response DTOs.
- `agentgo-dto:agentgo-dto-models`: Request and response DTOs for user-owned model management.
- `agentgo-dto:agentgo-dto-mcp`: Request and response DTOs for user-owned MCP Streamable HTTP servers.
- `agentgo-dto:agentgo-dto-settings`: Request and response DTOs for per-user system settings.
- `agentgo-springboot-starter`: Reusable Spring Boot auto-configuration, foundational beans,
  and shared infrastructure configuration.
- `db-migration`: Versioned Flyway SQL migrations loaded by `agentgo-app` at startup.

  base or aggregate registry. `ErrorResponseCodeRegistry` may aggregate error registries only
  for lookup and must not define domain-specific codes itself. Each registered code must define
  its meaning, category, and safe example message through `ResponseCodeDescription`.
- Maintain the canonical integer `code` to string `responseType` correspondence in
  `ResponseCodeRegistry.responseTypesByCode`. Every entry must resolve to a registered domain
  response type and must never be reused for a different outcome.
- For a dedicated module exception class with one fixed error status, register its
  `ExceptionClass → (code, responseType)` mapping with
  `ExceptionResponseCodeRegistry.register(...)` during module initialization. The registry
  rejects unregistered or mismatched Code pairs. Exceptions that intentionally carry different
  statuses per instance must expose their registered `code` and `responseType` explicitly.
- Use Actuator for health and operational endpoints; do not add a custom health controller.
- Keep application capability dependencies in `agentgo-app-modules`; `agentgo-app` should
  compose those starters rather than re-declaring their infrastructure dependencies.
- Add database changes only as new Flyway migrations under
  `db-migration/src/main/resources/db/migration/` using `V<version>__<description>.sql`.
- Never edit a migration that has been applied to a shared environment; create a new version.
- Document externally visible API changes in `README.md` and `CHANGELOG.md` when appropriate.
- Never commit secrets, credentials, tokens, private keys, or production data.
- Keep changes focused and preserve unrelated user work.

## Version and dependency changes

When adding or upgrading a dependency:

1. Define or update its version in the root `build.gradle.kts`.
2. Use versionless dependency declarations in module build files when dependency management
   provides the version.
3. Update documentation or `CHANGELOG.md` if the change affects users or release artifacts.
4. Run tests, coverage verification, and the full build.

## Git and pull request workflow

The intended flow is:

```text
main -> release/* -> feature/* or fix/* -> PR -> release/* -> PR -> main
```

- `main` and `release/*` are protected and must not receive direct commits.
- Pull requests must pass Merge CI, Commit Review, Dependency Review, and Review Policy.
- Pull requests created by the repository owner may pass Review Policy automatically.
- Pull requests created by other contributors require an approval from a reviewer other than
  the author.
- Use the repository PR template and complete every applicable section.
- Use the issue templates for bug reports and feature requests.
- Keep the PR description in English and document release impact and validation results.

Commit messages must use the format:

```text
<emoji><type>: <message>
```

Each commit must change fewer than 300 lines. Examples:

```text
✨feat: add agent workflow endpoint
🐛fix: handle missing request identifier
📝docs: update release instructions
```

## Security and quality

Security-sensitive changes require extra care and must not weaken repository protections.
The repository uses security advisories, secret scanning, dependency review, CodeQL, and
private vulnerability reporting. Report vulnerabilities through the repository security
advisory process rather than opening a public issue.

Before handoff, verify:

- `git diff --check` passes.
- Tests and `koverVerify` pass.
- `gradle build` succeeds.
- Only the two intended executable Jars are release artifacts.
- Documentation and changelog entries are updated when required.
- The working tree contains only intentional changes.

## Branch governance and AgentGo Project

All branch planning uses [AgentGo GitHub Project #2](https://github.com/users/Martin-WMM/projects/2).
This repository is linked to that shared Project; do not create a separate planning board.

```text
main -> release/<name> -> feature/<issue-number>-<name> or fix/<issue-number>-<name>
     feature/fix -> PR -> their source release/<name> -> PR -> main
main -> hotfix/<issue-number>-<name> -> PR -> main
```

- Create `release/*` and `hotfix/*` from an up-to-date `origin/main`.
- Create `feature/*` and `fix/*` from the intended, up-to-date `origin/release/*`, never directly from `main`.
- A PR into `release/*` must come from `feature/*` or `fix/*` created for that release.
- A PR into `main` must come from `release/*` or `hotfix/*`; feature/fix branches cannot target `main`.
- `main` and every `release/*` prohibit deletion, force pushes, and direct pushes. Change them only through PRs with all required checks passing.
- Retain `release/*` permanently, including after promotion to `main`. Never remove or bypass their deletion protection for cleanup.
- Disable repository-wide automatic head-branch deletion. Cleanup may delete only merged `feature/*`, `fix/*`, and `hotfix/*`.
- After merging a hotfix to `main`, synchronize active release branches through a project-tracked `fix/*` PR based on each affected release; do not push synchronization commits directly.

Before creating any release, feature, fix, or hotfix branch:

1. Create a repository Issue with context, expected outcome, acceptance criteria, and labels.
2. Add it to AgentGo Project #2 and set `Branch`, `Source branch`, `Target branch`, and `Status`.
3. For a release, record `Source branch = main` and `Target branch = main`; for feature/fix, record the same source and target release; for hotfix, record `main` as both.
4. Set `Status = In Progress` when work starts. Create the branch only after verifying Project membership with the authenticated GitHub CLI.
5. Add every associated PR to the same Project, populate its branch fields, and link the Issue using `Closes #<number>`.
6. Set the Issue and PR items to `Done` only when their acceptance criteria are met; retain release planning items and branches.

Each PR body must include the following machine-readable lines in addition to the repository template:

```text
Closes #<issue-number>
Project: https://github.com/users/Martin-WMM/projects/2
Source branch: <main-or-release/name>
```

The required `Branch flow policy` check validates permitted source/target branch types, issue naming and links, the declared source branch, and Git ancestry. Git does not record which checkout command created a branch; agents must verify the Project's source-branch field before creation. The PR check validates the Project declaration; actual Project membership and item fields must be verified with `gh project` during planning and review. Do not claim that a URL alone proves membership.

Use `gh project item-add 2 --owner Martin-WMM --url <issue-or-pr-url>` and `gh project item-edit` to manage items. GitHub Actions' repository token does not provide user-Project automation permissions; never copy an interactive login credential into Actions secrets. Automated Project membership checks require a separately provisioned Projects credential.
