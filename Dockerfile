# First stage: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# Second stage: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the specific jar from builder stage
COPY --from=builder /app/build/libs/api-gateway.jar api-gateway.jar

# Change ownership
RUN chown spring:spring api-gateway.jar

USER spring

# Set active profile
ENV SPRING_PROFILES_ACTIVE=docker

HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:9090/actuator/health || exit 1

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]