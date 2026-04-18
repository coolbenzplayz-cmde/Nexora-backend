FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle

RUN sed -i 's/\r//' gradlew && chmod +x gradlew
RUN ./gradlew --no-daemon --version

COPY src src

RUN ./gradlew --no-daemon bootJar -x test

RUN mkdir -p build/extracted \
 && (cd build/extracted && java -Djarmode=layertools -jar ../libs/nexora-1.0.0.jar extract)

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build --chown=spring:spring /workspace/build/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /workspace/build/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]