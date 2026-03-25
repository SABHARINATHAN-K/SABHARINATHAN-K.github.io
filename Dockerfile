# 1. Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code FROM the BackEnd folder
COPY BackEnd/pom.xml .
COPY BackEnd/src ./src

# Build the application
RUN mvn clean package -DskipTests

# 2. Run Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]