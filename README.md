# projector

Diagrams as code: write UML **activity diagrams** as YAML and render them to **SVG**
in the browser. Diagrams can be saved and reloaded.

- **Backend:** Spring Boot 4.0.6, Java 25 — parses YAML, lays out with ELK, emits SVG,
  stores diagrams in MongoDB.
- **Frontend:** React + TypeScript (Vite), Monaco editor + live SVG preview.

See [PLAN.md](PLAN.md) for the full MVP plan and the per-step breakdown.

## Prerequisites

- Java 25 (JDK)
- Maven (or use the bundled `./mvnw`)
- Node.js 24+ and npm
- Docker (for MongoDB via `spring-boot-docker-compose`)

## Run (development)

The backend and frontend run as two processes during development.

**1. Backend** (auto-starts a MongoDB container via `compose.yaml`):

```bash
./mvnw spring-boot:run
```

Serves on http://localhost:8080. Health check: `curl http://localhost:8080/api/health`
→ `{"status":"ok"}`.

**2. Frontend** (Vite dev server, proxies `/api` → backend on :8080):

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open the URL Vite prints (default http://localhost:5173): the projector editor, with a
Monaco YAML pane, live SVG preview, and the saved-diagrams sidebar.

## Build

```bash
./mvnw clean package          # builds the frontend AND the backend into one jar
```

`frontend-maven-plugin` installs a pinned Node, runs `npm ci` + `npm run build`, and the
Vite output lands in `target/classes/static`, so the jar serves the UI from
`classpath:/static/`. Skip the frontend build with `-Dfrontend.skip=true` (backend only).

## Run (production)

The packaged jar serves the UI and the API on one port and is fully self-contained
(Monaco is bundled, no CDN). Unlike `spring-boot:run`, it does **not** auto-start
MongoDB — point it at a real one:

```bash
SPRING_MONGODB_URI=mongodb://localhost:27017/projector \
  java -jar target/projector-1.0.0-SNAPSHOT.jar
```

Then open http://localhost:8080.

## Tests

```bash
./mvnw test                   # backend unit tests (no Docker needed)
./mvnw verify                 # also runs integration tests (*IT)
cd frontend && npm run build  # type-checks the frontend
```

`./mvnw verify` runs the persistence integration tests, which use
`docker-maven-plugin` to start a throwaway MongoDB container on host port **27018**
(and stop it afterwards), so Docker must be available and that port free.
