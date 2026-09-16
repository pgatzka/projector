# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Always use the Maven wrapper (`./mvnw`), not a system `mvn`.

- Build: `./mvnw package`
- Run app: `./mvnw spring-boot:run` (serves on `localhost:8080`)
- All tests: `./mvnw test`
- Tests + coverage gate + format check: `./mvnw verify` (JaCoCo report at `target/site/jacoco/index.html`)
- Format code: `./mvnw spotless:apply` (Palantir Java Format); `verify` fails on unformatted code via `spotless:check`
- Single test: `./mvnw test -Dtest=HelloControllerTest` or `-Dtest=HelloControllerTest#hello`

## Git workflow

- `main` is protected: never push to it directly. Every change goes on a branch and through a pull request (`gh pr create`).
- Never merge a pull request yourself. Only the user merges; if merging seems needed, ask for permission first.

## Stack

- Java 25, set via `java.version` in `pom.xml` (the Spring Boot parent maps it to the compiler release).
- Spring Boot 4.1.x via `spring-boot-starter-parent`.
- Lombok, set up per its official Maven docs: `provided` scope plus `annotationProcessorPaths` in `maven-compiler-plugin` (mandatory on JDK 23+, which no longer runs processors implicitly). Version is Boot-managed via `${lombok.version}`.
- JaCoCo: `check` fails `verify` below 80% line and branch coverage (bundle-wide). `ProjectorApplication` is excluded from coverage.
- Surefire loads Mockito as a `-javaagent` (per Mockito docs, JDK 21+ restricts self-attach), resolved via `maven-dependency-plugin:properties`. Its `argLine` starts with `@{argLine}` so JaCoCo's agent is kept; the empty `<argLine/>` property keeps it valid when JaCoCo is skipped.
- Test logging: `src/test/resources/logback-test.xml` sends all logs to `target/test.log` (overwritten each run), not the console. Check that file when debugging test failures.
- Maven coordinates `io.github.pgatzka:projector`; base package `io.github.pgatzka.projector`.

## Spring Boot 4 specifics

Boot 4 split the starters into modules; older names/packages from Boot 3 do not apply:

- Web uses `spring-boot-starter-webmvc` (not `spring-boot-starter-web`); tests use `spring-boot-starter-webmvc-test` (not `spring-boot-starter-test`). These names are expected to change again in future releases, so check current Boot docs before adding starters.
- Test slice annotations moved, e.g. `@WebMvcTest` is `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`.
- Controller and service are tested separately. Controller tests use `@WebMvcTest` + `MockMvcTester` (AssertJ style) with the service replaced by `@MockitoBean` (see `HelloControllerTest`); service tests are plain unit tests without a Spring context (see `HelloServiceTest`).

## Project conventions

- Write modern Java 25 syntax using finalized (non-preview) features: records, sealed types, switch expressions, pattern matching for `instanceof`/`switch` and record deconstruction, text blocks, unnamed variables `_`, flexible constructor bodies (statements before `super(...)`), module import declarations, `SequencedCollection` methods (`getFirst()`/`getLast()`). Don't enable preview features.
- Don't use `var`; always declare explicit types.
- Name variables, fields and parameters after what they are or do (`mockMvcTester`, not `mvc`). Only conventional short names like `i` in a `for` loop are fine.
- `.gitignore` / `.gitattributes`: a line earns its place only if it's actually required now, i.e. the matching files exist in this repo (no `.idea/` entry without `.idea/` files, no `*.jar` rule without jars). Add entries when such files appear; remove them when they're gone.
- Controllers are a thin layer: consume the request and delegate to the controller's own service (`HelloController` → `HelloService`). Every handler method returns `ResponseEntity<T>`, using `ResponseEntity<Void>` when there is no body. Services return plain `T` and never `ResponseEntity`.
- Spring configuration is YAML (`application.yaml`), never `.properties` files.
