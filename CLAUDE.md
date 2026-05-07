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

## Labels + filtering + FTS (v0.4 onwards)
- Label color is a Postgres native enum `label_color` with 12 values: `gray, red, orange, yellow, green, teal, blue, indigo, violet, pink, brown, slate`. Tailwind has no `brown` — `LabelBadge` maps it to `amber` swatches.
- Label name is unique per project, **case-insensitive** (`unique index on (project_id, lower(name))`).
- Label management UI lives at `/projects/{KEY}/labels`. Inline create/edit form, swatch-based color picker, delete confirms with assignment count fetched via `GET /api/projects/{KEY}/issues?label=<id>&size=1` (using the `total` field).
- Issue list endpoint `GET /api/projects/{KEY}/issues` returns a wrapped page: `{items: IssueDto[], total, page, size}`. Sort is `number desc` (no sort param yet). Default size 50, max 100 (server-side clamp).
- Filter facets: `?status=todo,in_progress&priority=high,urgent&label=<uuid>,<uuid>&q=login&page=2&size=50`. **OR within a facet, AND across facets.** Multi-value uses comma separation. `q` is FTS via `websearch_to_tsquery('english', q)` against the generated `issue.search_tsv`.
- `IssueDto` embeds `labels: [{id, name, color}]` — always present (possibly empty). Single batch query `loadLabelsByIssueIds` populates them via `fetchGroups`.
- Label assignment is per-label and idempotent: `POST /api/projects/{KEY}/issues/{N}/labels` with body `{labelId}` (200 even if already assigned), `DELETE /api/projects/{KEY}/issues/{N}/labels/{labelId}` (204 even if not assigned). Cross-project label → 400 `LabelNotInProjectException`.
- `CreateIssueRequest.labelIds` (optional `List<UUID>`) — validated and bulk-inserted in the same transaction as the issue.
- Frontend filter/search/page state is persisted via URL query params (`useIssueListQuery` hook). Filter changes reset `page` to 0.
- IssueDetail uses immediate-on-toggle assignment (one POST/DELETE per checkbox click); IssueForm in edit mode diffs the label set against the original and POSTs/DELETEs the deltas before the field PATCH.

## Comments + activity + markdown (v0.5 onwards)
- **Comments are append-only.** No PATCH, no DELETE endpoint on individual comments. They cascade away when their issue (or the issue's project) is deleted.
- Comment body capped at **10000 chars** server-side (`@Size(max=10000)` + `varchar(10000)` column).
- `comment.search_tsv` is `to_tsvector('english', body_md)` GIN-indexed. Issue list FTS (`?q=`) matches when EITHER `issue.search_tsv` matches OR `exists (select 1 from comment where comment.issue_id = issue.id and comment.search_tsv @@ websearch_to_tsquery('english', q))`.
- **Activity emission lives in `ActivityService` (rest.service facade) → `ActivityDataService.emit(issueId, action, payload)`.** Wired into `IssueService` (issue_created on create; one row per changed field on update) and `IssueLabelService` (label_added/removed). 8 actions: `issue_created, status_changed, priority_changed, due_date_changed, title_edited, description_edited, label_added, label_removed` (Postgres native enum `activity_action`).
- **Activity payload is tombstone-resilient.** Snapshot before/after primitive values; for label entities, payload carries `{labelId, labelName, labelColor}` so renaming or deleting the label later doesn't corrupt the historical row.
- **`ActivityService` ↔ `IssueService` circular dep is broken with `@Lazy` on the ActivityService injection** in IssueService and IssueLabelService. Cleaner refactor (extract issue-id lookup so ActivityService doesn't depend on IssueService) is v0.6+ tech debt.
- **`ObjectMapper` is NOT a default Spring bean under `spring-boot-starter-webmvc`.** Use `private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()` inside services that need JSONB serialization, NOT constructor injection. See `ActivityDataService` and `ActivityDto`.
- Timeline endpoint `GET /api/projects/{KEY}/issues/{N}/timeline` returns `{entries: [{type:"comment"|"activity", ...}], total}`, sorted `created_at asc` (oldest first, GitHub-style). Single fetch — no pagination in v0.5.
- `TimelineEntryDto` is a single record class with nullable `bodyMd`/`action`/`payload` fields + `@JsonInclude(NON_NULL)` for clean wire shape (chosen over sealed interface for Jackson simplicity).
- Frontend renders markdown via `react-markdown` + `remark-gfm` + `rehype-sanitize` (allowlist: GFM tables/checkboxes/del; href limited to http/https/mailto; `<a>` overridden with `target="_blank" rel="noopener noreferrer"`). Component lives at `frontend/src/components/Markdown.tsx`. Uses Tailwind `prose` classes (requires `@tailwindcss/typography` — installed v0.5).
- IssueDetail mounts `<IssueTimeline>` below the metadata block; description is rendered via `<Markdown>` (replaced v0.3's `<pre>`).
- CommentComposer is single-pane Write/Preview toggle (no side-by-side), explicit submit, char counter that turns red on overflow. Refresh on submit goes through react-query invalidation via parent's `onCreated`.

## Kanban board + polish (v1.0 onwards)
- Drag-drop board lives at `/projects/{KEY}/board`. Columns are the 5 issue statuses in order: `backlog, todo, in_progress, done, cancelled`. Within-column sort is `number desc` (no manual ordering — drag is status-only).
- DnD via `@dnd-kit/core` + `@dnd-kit/sortable` + `@dnd-kit/utilities`. `PointerSensor` activation distance = 8px so click-to-open and drag are unambiguous on the same target. `KeyboardSensor` wired for accessibility.
- Drop logic: optimistic local cache mutation via `useBoardData.moveIssueOptimistic(...)` returning a `revert` snapshot, then `issuesApi.update(key, n, {status})`. On 2xx: `queryClient.invalidateQueries(['projects', key, 'board'])`. On error: revert + push toast via `useToast()`. The status_changed activity row emits server-side from v0.5 wiring — no extra work.
- Cards: identifier (muted) + title (line-clamp-2) + label badges + priority pill (hidden when `medium`) + due-date relative chip (only when set AND ≤7 days, includes "overdue"). Whole card is the drag handle; click navigates to issue detail.
- Filters on the board reuse `IssueListFilters` + `useIssueListQuery`. Status filter is intentionally STRIPPED from the API call (board always renders all 5 columns) but preserved in URL state so toggling back to List view keeps user's selection. Other facets (label, priority, q) flow through.
- **Issue list endpoint cap of 100 affects the board.** A project with >100 issues across all statuses sees only the most recent on the board. Address in v1.1+ if it bites.
- Toast: hand-rolled `ToastProvider` + `useToast` hook in `frontend/src/components/Toast.tsx`. No new dep. Auto-dismisses after 5s, click-to-dismiss, portaled to body, fixed bottom-right. Mounted at app root in `main.tsx`.
- KEY-N issue autolinks: custom remark plugin `frontend/src/utils/remarkKeyN.ts` runs in the `Markdown.tsx` pipeline BEFORE `remarkGfm`. Pattern `\b[A-Z]{2,10}-\d+\b`, skips `inlineCode`/`code` parents. Generates internal links `/projects/{KEY}/issues/{N}` rendered via react-router `<Link>` (Markdown's `<a>` override routes paths starting with `/` to `<Link>` and external URLs to `<a target="_blank" rel="noopener noreferrer">`).
- **Mobile policy:** the kanban board requires viewport ≥768px (Tailwind `md`). Below that, `MobileNotSupportedBanner` replaces the board entirely with a link back to the list view. Defensive: PointerSensor distance forced to 99999 below md so dragging is inert even if the layout somehow renders. Other pages degrade to single-column layouts but stay usable on tablet.
- **No version bump for v1.0** — pom.xml stays `1.0.0-SNAPSHOT`. No `v1.0.0` git tag. v1.0 is the milestone name, not a cut release.

## jOOQ gotcha — Postgres `GENERATED ALWAYS AS … STORED` columns
- jOOQ 3.19 OSS does **not** auto-detect Postgres generated columns as readonly. The codegen `<syntheticObjects><readonlyColumns>` config marks the field `@Deprecated` but doesn't exclude it from `record.store()` SQL. Excluding the column via `<excludes>` regex doesn't work for individual columns either.
- **Workaround:** in any repository method that does `dsl.newRecord(TABLE, pojo).store()` or `.update()` on a table with a generated column, call `record.changed(TABLE.GENERATED_FIELD, false)` BEFORE the store. See `IssueRepository.insert/update` for `ISSUE.SEARCH_TSV` and `CommentRepository.insert` for `COMMENT.SEARCH_TSV`.

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
