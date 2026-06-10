# projector — MVP Plan

A web app where you write a UML **activity diagram** as YAML and get a live-rendered
**SVG**. Diagrams can be **saved and reloaded**. No auth.

---

## Locked decisions

| Area | Decision |
|---|---|
| Diagram type | UML activity diagram |
| Interface | Web app: React + TypeScript frontend, Spring Boot backend |
| DSL | Custom YAML syntax (explicit `nodes` + `edges`) |
| Elements | start/end, action+flow, decision/merge, fork/join |
| Output | SVG |
| Render side | Backend: Java parses YAML → graph model → layout → SVG |
| Layout engine | ELK (Eclipse Layout Kernel, pure Java), `layered` algorithm, top-down |
| Storage | MongoDB, one document per diagram |
| Persistence | Save / load / list / delete. **No auth.** |
| Build tool | Maven |
| Maven coordinates | groupId `io.github.pgatzka`, artifactId `projector`, version `1.0.0-SNAPSHOT` |
| Base package | `io.github.pgatzka.projector` |
| Production serving | Spring Boot serves the frontend (Vite build → `static/`), one deployable |
| Java | 25 (LTS) |
| Spring Boot | 4.0.6 (GA; supports Java 25). NB: Initializr emits the legacy `4.0.6.RELEASE` id — the real artifact version is `4.0.6` |
| Code editor | Monaco (`@monaco-editor/react`), the engine behind VS Code |
| Mongo runtime | `spring-boot-docker-compose` + `compose.yaml` (requires Docker) |
| Repo layout | Root = Spring Boot app; `/frontend` holds the React app |
| Git | User runs `git init` |

### Render flow
React `POST`s YAML to the backend (debounced) → backend returns SVG → React displays it.

### Proposed YAML syntax
```yaml
diagram: Order Processing
nodes:
  - { id: start,     type: start }
  - { id: receive,   type: action,   label: Receive order }
  - { id: check,     type: decision, label: In stock? }
  - { id: fork1,     type: fork }
  - { id: ship,      type: action,   label: Ship order }
  - { id: invoice,   type: action,   label: Send invoice }
  - { id: join1,     type: join }
  - { id: backorder, type: action,   label: Backorder }
  - { id: done,      type: end }
edges:
  - { from: start,   to: receive }
  - { from: receive, to: check }
  - { from: check,   to: fork1,     guard: "in stock" }
  - { from: check,   to: backorder, guard: "out of stock" }
  - { from: fork1,   to: ship }
  - { from: fork1,   to: invoice }
  - { from: ship,    to: join1 }
  - { from: invoice, to: join1 }
  - { from: join1,   to: done }
  - { from: backorder, to: done }
```

**Shapes:** start = filled circle, end = filled circle in ring, action = rounded box,
decision/merge = diamond, fork/join = solid bar. `guard` renders as an edge label.

---

## Scope

**In:** the five element groups; YAML parse + validation with useful errors; ELK layout;
SVG generation; save/load/list/delete; single-page web UI.

**Out:** auth/users, swimlanes, real-time collaboration, PNG/PDF export, undo history,
terser DSL, custom Monaco language/schema validation.

---

## Success measures (defined before coding)

- Parser unit tests: valid YAML → correct model; malformed YAML / unknown node type /
  dangling edge → clear errors.
- Layout/SVG test: the sample above → SVG containing the expected shapes and edge labels.
- API integration test: `POST /api/render` (yaml) → 200 + SVG; diagram CRUD round-trips
  through MongoDB.
- Manual acceptance: type the sample in the UI, see it render, save it, reload it, delete it.

---

## Cross-cutting decisions still open

These affect multiple steps and should be settled early (called out again per-step):

- **Mongo in tests:** Testcontainers (real Mongo in Docker) vs. flapdoodle embedded Mongo.
  (App runtime uses real Mongo via docker-compose; tests are a separate choice.)

---

## Steps

### Step 1 — Scaffold

**Key changes**
- Maven `pom.xml`: Spring Boot (version supporting Java 25), Java 25 toolchain,
  `spring-boot-starter-web`, `spring-boot-starter-data-mongodb`,
  `spring-boot-docker-compose`.
- `compose.yaml` with a `mongo` service.
- React + TypeScript app via Vite under `/frontend`, with dev proxy to the backend.
- `.gitignore`, minimal health endpoint, README run notes.

**Done when**
- `./mvnw spring-boot:run` boots the backend and auto-starts the Mongo container.
- `npm run dev` serves the frontend; it can reach a backend health endpoint through the proxy.

**Decisions to make**
- Pinned Spring Boot version. **If no Spring Boot release supports Java 25 cleanly: STOP
  and report — do not silently fall back to Java 21.**
- (Resolved: root = Spring Boot app, `/frontend` = React; Spring Boot serves the built
  frontend from `static/`; coordinates `io.github.pgatzka:projector:1.0.0-SNAPSHOT`,
  package `io.github.pgatzka.projector`.)

---

### Step 2 — Domain model + YAML parser

**Key changes**
- Java model: `Diagram`, `Node` (+ `NodeType` enum), `Edge`.
- YAML parser into the model.
- Validation: unknown node type, edge referencing a missing id, duplicate ids,
  structural rules (e.g. start/end presence).

**Done when**
- Unit tests pass: valid sample → correct model; malformed YAML / unknown type /
  dangling edge → specific, clear errors.

**Decisions to make**
- Parser library: Jackson `dataformat-yaml` vs SnakeYAML.
- Validation strictness: require exactly one start? allow multiple ends? require all nodes
  reachable?
- Error model: exception types and message format surfaced to the API.
- ID rules (allowed characters, case sensitivity).

---

### Step 3 — Model → ELK → layout

**Key changes**
- Add ELK dependency.
- Map the model to an ELK graph; configure `layered` algorithm, direction `DOWN`, spacing.
- Run layout; extract node coordinates/sizes and edge bend points.

**Done when**
- Unit test: the sample yields non-overlapping nodes with coordinates and edges with
  routing points; output is deterministic across runs.

**Decisions to make**
- ELK version.
- Node sizing: fixed sizes per type vs. sizing action boxes to label text length.
- Edge routing style: `ORTHOGONAL` vs `POLYLINE`.
- Spacing values.

---

### Step 4 — SVG generator

**Key changes**
- Build SVG from laid-out coordinates: shape per node type, edges with arrowheads,
  guard labels, `viewBox` from layout bounds.

**Done when**
- Unit test: SVG string contains the expected shapes and edge labels for the sample.
- Visual inspection of the rendered sample looks correct.

**Decisions to make**
- Styling: inline attributes vs embedded CSS; colors, fonts, stroke widths.
- Arrowhead approach (SVG `marker`).
- Text measurement consistency between layout sizing (Step 3) and SVG rendering.

---

### Step 5 — `POST /api/render` endpoint

**Key changes**
- REST controller: accept YAML, return SVG. Map parse/validation errors to error responses.

**Done when**
- Integration test (MockMvc): valid YAML → 200 + SVG; invalid YAML → 4xx + error body.

**Decisions to make**
- Request/response content types: raw `text/plain` + `image/svg+xml` vs JSON envelopes.
- Error response schema (status codes + body shape).
- CORS config for dev.

---

### Step 6 — MongoDB persistence + CRUD

**Key changes**
- `Diagram` document (id, name, yaml, created/updated timestamps).
- Spring Data Mongo repository + service.
- CRUD endpoints: create, list, get by id, update, delete.

**Done when**
- Tests round-trip a diagram through Mongo.
- Manual `curl` create → list → get → delete works against the docker-compose Mongo.

**Decisions to make**
- What to store: raw YAML only, or also parsed model / cached SVG.
- ID strategy: Mongo `ObjectId` vs name-as-key; name uniqueness.
- Update semantics (PUT replace vs partial).
- Test Mongo: Testcontainers vs flapdoodle embedded.

---

### Step 7 — React UI

**Key changes**
- Monaco editor pane + live SVG preview pane.
- Debounced call to `/api/render`.
- Diagram list/sidebar with save / load / delete and a name field.
- Error display for parse/validation failures.

**Done when**
- Manual: type the sample → see it render; save → appears in list; reload → editor
  repopulates and renders; delete → removed. Debounce avoids a request per keystroke.

**Decisions to make**
- State management: local React state vs a library (likely local for MVP).
- SVG injection: `dangerouslySetInnerHTML` vs `<img>`.
- Save UX: new vs overwrite existing.
- Styling approach: plain CSS vs Tailwind vs component library.

---

### Step 8 — End-to-end wiring + acceptance

**Key changes**
- Finalize production serving (per the cross-cutting decision).
- README with run instructions.
- Full acceptance run of the manual scenario.

**Done when**
- The manual acceptance scenario passes start to finish from a clean checkout.

**Decisions to make**
- Final production serving model (if not already settled in Step 1).
- README contents.
