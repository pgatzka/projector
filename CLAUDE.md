# Projector — Project Preferences

This file is the source of truth for durable conventions in this repo. Read before suggesting changes that touch the build, schema, packaging, or deployment.

## Stack constraints (non-negotiable)
- **Java 21**, **Spring Boot 4.0.6**, **Maven** (single module at repo root)
- **jOOQ** for DB access — **no JPA, no Hibernate**
- **Postgres 18** only (image: `postgres:18-alpine`)
- **`spring-boot-starter-webmvc`** (NOT `spring-boot-starter-web`)
- **`spring-boot-starter-flyway`** (NOT `flyway-core` directly)
- **Single Docker container**: Spring Boot serves both API and the bundled SPA from `src/main/resources/static/`
- **The jar is built by Maven, then copied into the runtime image.** The Dockerfile must NOT compile Java or build the frontend.

## Packaging
- Layer-based: `config/`, `audit/`, `data.repository/`, `data.service/`, `rest.controller/`, `rest.service/`, `rest.dto/`, `rest.exception/`. **No domain packages.**
- Base package: `io.github.pgatzka.projector`
- Generated jOOQ sources live under `target/generated-sources/jooq` (gitignored). Generated package: `io.github.pgatzka.projector.jooq`.
- **Use jOOQ-generated POJOs.** Do not hand-write POJOs that mirror tables.

## Layer rules
- **Controllers never call repositories.** Controller → `rest.service` → `data.service` (or `data.repository`).
- **Business logic lives in `rest.service`.** `data.service` is transactional CRUD per aggregate, no rules.

## Database conventions
- **All SQL is lowercase.** Identifiers are `snake_case`.
- **Table names are singular.** The users table is named `account` (not `user` — reserved keyword).
- **Mandatory column block on every table** (in this order):
  ```sql
  id          uuid                     not null default uuidv7() primary key,
  created_at  timestamp with time zone not null default current_timestamp,
  created_by  varchar(64)              not null,
  updated_at  timestamp with time zone not null default current_timestamp,
  updated_by  varchar(64)              not null,
  version     integer                  not null default 0,
  ```
- **Audit actor format**: `created_by` and `updated_by` are `varchar(64) not null`, application-enforced format `"user:<uuid>"`. **No FK** — actor prefixes (`user:`, `system:`, `job:`) are first-class.
- `set_updated_at()` trigger ignores `updated_at`, `updated_by`, and `version` when deciding whether to bump `updated_at`.
- **`AuditRecordListener`** sets `created_by`/`updated_by` automatically on insert/update — services must not set them manually.
- **Flyway** for migrations. One SQL statement per file. Filename shape per the database-design skill (`V<n>__create_table_<name>.sql`, etc.).
- **Whenever working on the database, the `database-design` skill MUST be used.**

## Auth (v0.2 onwards)
- Single admin account, created via `/api/setup` when account table is empty (returns 409 afterwards)
- bcrypt password hashing via `PasswordEncoder` bean
- Spring Security session cookie with 30-day idle timeout (in-memory; lost on restart)
- CSRF: double-submit cookie (`XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header). `/api/setup`, `/api/login`, `/api/logout` exempt.
- `ActorContext` reads from `SecurityContextHolder`; `SetupActorOverride` bridges the chicken-and-egg of `/api/setup`
- `POST /api/login` returns 200 + sets cookie. `POST /api/logout` invalidates session, returns 204. `GET /api/me` returns 200/401.

## Projects + Issues (v0.3 onwards)
- `project.key` is 2–10 uppercase letters, must start with a letter; unique. Validated app-side.
- Issue identifier is `<project.key>-<issue.number>`. `issue.number` is per-project, claimed via row-locked increment of `project.next_issue_number` inside the create transaction.
- Status enum: `backlog`, `todo`, `in_progress`, `done`, `cancelled` (Postgres native enum `issue_status`)
- Priority enum: `low`, `medium`, `high`, `urgent` (Postgres native enum `issue_priority`)
- Hard delete with `on delete cascade`: deleting a project deletes its issues; deleting an issue cascades v0.4 labels and v0.5 comments/activity.
- REST: `/api/projects` (list/create), `/api/projects/{key}` (get/patch/delete), `/api/projects/{key}/issues` (list/create), `/api/projects/{key}/issues/{number}` (get/patch/delete).
- Frontend routes: `/projects`, `/projects/new`, `/projects/{KEY}`, `/projects/{KEY}/edit`, `/projects/{KEY}/issues/new`, `/projects/{KEY}/issues/{N}`, `/projects/{KEY}/issues/{N}/edit`.
- Issue list default sort: `number desc` (newest first).
- Save model: explicit Save button. PATCH with non-null fields only. Activity log (v0.5) writes one row per save.
- v0.3 renders issue description as `<pre>` markdown text. Real markdown rendering arrives in v0.5 with comments.
- CSRF cookie: a `CsrfCookieFilter` (in `config/`) materializes the XSRF-TOKEN cookie eagerly on every response. Required for SS6+/7's deferred token loading; without it, the SPA would never receive a token after login (login is CSRF-exempt).

## Frontend conventions
- Vite + React + TypeScript + Tailwind, `src/` rooted at `frontend/`
- Frontend `package.json` version is kept in lockstep with `pom.xml` via `npm pkg set version=${project.version}` during the Maven `validate` phase.
- **Whenever working on the frontend, the `frontend-design` skill MUST be used.**

## Build & local dev
- `compose.yaml` at repo root provides `postgres:18-alpine` for local dev. `spring-boot-docker-compose` auto-discovers it on `mvn spring-boot:run`.
- **No `SPRING_DATASOURCE_*` env vars in any config we own.** Spring reads from the running Postgres container (dev) or the operator's deployment env (prod).

## Testing
- **Unit tests**: `*Test.java`, surefire, **no Spring context**.
- **E2E tests**: `*IT.java`, failsafe, Cucumber 7 (Java + cucumber-spring + JUnit Platform Suite). Step glue: `io.github.pgatzka.projector.e2e`.
- A separate `postgres:18-alpine` test container is started by `docker-maven-plugin` on `pre-integration-test` (port 25432) and stopped on `post-integration-test`. Migrations run via `flyway-maven-plugin`.

## CI / image publishing
- GitHub Actions runs `mvn verify` and builds the Docker image on every push/PR.
- On `main` only, the image is pushed to GHCR with **two tags**: `${project.version}-${shortSha}` and `${project.version}`.
- **Never `:latest`.**

## Where things live
- Plans: `docs/superpowers/plans/`
- Reference codegen pom (kept for diffs): `docs/jooq-codegen-example/pom.xml`
