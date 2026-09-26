# Database migrations

This directory contains the versioned Flyway migrations used by `agentgo-app`.

Migration files belong in:

```text
db-migration/src/main/resources/db/migration/
```

Use Flyway's naming convention:

```text
V<version>__<description>.sql
```

Examples:

```text
V1__initialize_agentgo_schema.sql
V2__create_agent_runs.sql
```

Never edit a migration that has already been applied in a shared environment. Add a new
versioned migration instead. The application runs Flyway migrations during startup before
Hibernate validates the schema.
