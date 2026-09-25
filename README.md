# AgentGo Backend

AgentGo Backend is a Kotlin, Gradle, Spring MVC, and Spring Boot 4 application.

## Development workflow

Code changes follow: `main → release/* → feature/* or fix/* → PR → release/* → PR → main`.

- `main` and `release/*` accept changes only through pull requests.
- Every commit must use `<emoji><type>: <message>` and change fewer than 300 lines.
- Every pull request must pass Merge CI, tests, and the 85% code coverage gate.

## Local build

Install JDK 21 and run:

```bash
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

## Application

- `agentgo-app` is the only project module.
- It provides the Spring MVC HTTP API.
- The baseline uses Spring Boot 4.1.1, Kotlin 2.2.x, Java 21, and Gradle 8.14+.

## Health endpoint

```text
GET /api/v1/health
```
