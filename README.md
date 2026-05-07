# Projector

Solo, self-hosted issue tracker. Single Docker container running Spring Boot 4 (REST API + bundled Vite/React SPA) backed by Postgres 18.

## Prerequisites
- JDK 21 (Temurin recommended)
- Maven 3.9+
- Docker (for local Postgres + image build)
- A running Postgres 18 instance (homeserver or container) for production

## Build, test, package
```bash
mvn verify
```
Runs: jOOQ codegen against a throwaway Postgres → unit tests → Cucumber E2E against a separate test Postgres → fat jar at `target/projector-1.0.0-SNAPSHOT.jar` (with the SPA bundled inside).

## Run locally
```bash
mvn spring-boot:run
```
`spring-boot-docker-compose` auto-starts `postgres:18-alpine` from `compose.yaml`. Open <http://localhost:8080>.

For frontend hot-reload, run Vite in parallel:
```bash
cd frontend && npm run dev
```
Then open <http://localhost:5173> (Vite proxies `/api` and `/actuator` to `:8080`).

## Build the Docker image
```bash
mvn package -DskipTests   # produce jar
docker build -t projector:dev .
```

## Run the image
```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<your-postgres-host>:5432/projector \
  -e SPRING_DATASOURCE_USERNAME=projector \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  projector:dev
```
Or include the app in your homeserver's compose stack alongside `postgres:18-alpine` and let `spring-boot-docker-compose` discover it.

## Pull the published image (no build required)
```bash
docker pull ghcr.io/pgatzka/projector:1.0.0-SNAPSHOT
```
Each `main` push tags both `${version}-${shortSha}` and `${version}` on GHCR. No `:latest`.

## First-time setup
Once the app is running, create the single admin account:
```bash
curl -X POST http://localhost:8080/api/setup \
  -H 'Content-Type: application/json' \
  -d '{"email":"you@example.com","password":"<choose-one>"}'
```
The `/api/setup` endpoint returns 409 once an account exists. Subsequent logins go through `POST /api/login`.

## Endpoints
- `GET /api/health`, `GET /actuator/health`, `GET /actuator/prometheus`
- `GET /v3/api-docs`, `GET /swagger-ui.html`
- `POST /api/setup`, `POST /api/login`, `POST /api/logout`, `GET /api/me`
- `/api/projects`, `/api/projects/{key}/issues`, `/api/projects/{key}/labels`
- `/api/projects/{key}/issues/{n}/comments`, `/api/projects/{key}/issues/{n}/timeline`
- `GET /` — SPA (list view, kanban board, issue detail, label management)

## Project conventions
See [`CLAUDE.md`](./CLAUDE.md). Particularly: layer packaging, mandatory audit columns, jOOQ-generated POJOs, no `:latest` image tag.

## License
MIT — see [`LICENSE`](./LICENSE).
