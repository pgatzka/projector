# Runtime-only image. The jar must be built first with `mvn package`.
# This Dockerfile does NOT compile Java or build the frontend.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user for safety
RUN addgroup -S projector && adduser -S projector -G projector
USER projector

# Copy the pre-built fat jar (built by Maven, already contains the SPA)
COPY --chown=projector:projector target/projector-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
