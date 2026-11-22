FROM gradle:9.1.0-jdk25 AS build

WORKDIR /workspace
COPY . .

RUN gradle :agentgo-app:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:25-jre

# Python 3 for AgentGo Bash / CLI tool script execution in local Compose.
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /workspace/agentgo-app/build/libs/agentgo-app-*.jar /app/agentgo-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/agentgo-app.jar"]
