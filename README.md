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

## Endpoints (v0.1)
- `GET /api/health` — application health (always 200, body `{"status":"ok"}`)
- `GET /actuator/health` — Spring health
- `GET /actuator/prometheus` — Prometheus scrape
- `GET /v3/api-docs` — OpenAPI spec
- `GET /swagger-ui.html` — Swagger UI
- `GET /` — SPA (placeholder)

## Project conventions
See [`CLAUDE.md`](./CLAUDE.md). Particularly: layer packaging, mandatory audit columns, jOOQ-generated POJOs, no `:latest` image tag.
