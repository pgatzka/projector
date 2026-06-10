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

Open the URL Vite prints (default http://localhost:5173). The page shows the backend
health status, confirming the frontend ↔ backend wire.

## Build

```bash
./mvnw clean package        # backend jar
cd frontend && npm run build  # frontend production bundle (dist/)
```

In production the backend serves the built frontend (wiring added in a later step), so
the same `/api` paths work without the dev proxy.

## Tests

```bash
./mvnw test                 # backend
cd frontend && npm run build  # type-checks the frontend
```
