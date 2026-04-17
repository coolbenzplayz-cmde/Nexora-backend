FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy everything into container
COPY . .

# Make Gradle wrapper executable
RUN chmod +x gradlew

# Build Spring Boot app (safe full build)
RUN ./gradlew clean build

# Expose backend port
EXPOSE 8080

# Run the generated JAR
CMD ["sh", "-c", "java -jar build/libs/*.jar"]
