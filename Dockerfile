# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Build Spring Boot fat JAR
RUN mvn -B -DskipTests clean package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy ONLY the runnable Spring Boot JAR
COPY --from=build /workspace/target/applepay-stripe-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
