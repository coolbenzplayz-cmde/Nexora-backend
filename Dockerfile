# syntax=docker/dockerfile:1.7

# -----------------------------------------------------------------------------
# Stage 1: Build the Spring Boot application with a full JDK.
#
# We copy the Gradle wrapper and build scripts first so that Docker can cache
# dependency resolution separately from the source code. A BuildKit cache mount
# is used for the Gradle user home so repeated builds reuse the dependency
# cache without baking it into the final image.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy only the files needed to resolve dependencies first (better layer cache)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon --version

# Now copy sources and build the Spring Boot jar. Tests are skipped here
# because they are (and should be) run separately in CI.
COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon bootJar -x test

# Extract the Spring Boot layered jar so each layer can be copied into the
# runtime image separately, maximising Docker layer cache reuse on redeploys.
RUN mkdir -p build/extracted \
 && (cd build/extracted && java -Djarmode=layertools -jar ../libs/*.jar extract)

# -----------------------------------------------------------------------------
# Stage 2: Minimal runtime image.
#
# Uses a JRE-only Alpine base (~180 MB) instead of the full JDK (~460 MB) and
# runs the application as a non-root user.
# -----------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the extracted Spring Boot layers in order of change frequency
# (least → most) so that application code changes don't bust the
# much larger dependency layers.
COPY --from=build --chown=spring:spring /workspace/build/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
