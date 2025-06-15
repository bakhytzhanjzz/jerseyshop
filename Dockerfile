FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY .. .
RUN mvn clean package -DskipTests

# Use a smaller JDK runtime for running the app
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
