FROM gradle:9.1.0-jdk25 AS build

WORKDIR /workspace
COPY . .

RUN gradle :agentgo-app:bootJar --no-daemon --stacktrace

FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /workspace/agentgo-app/build/libs/agentgo-app-*.jar /app/agentgo-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/agentgo-app.jar"]
