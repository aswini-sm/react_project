# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Setup the runtime environment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render will override this via environment variable PORT if needed)
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
