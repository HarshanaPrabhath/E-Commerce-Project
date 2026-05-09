# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -q
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -q

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
VOLUME /app/images
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
