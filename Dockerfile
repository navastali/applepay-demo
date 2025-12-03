# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY .mvn .mvn
# copy only pom to download deps fast (if you use Maven wrapper)
RUN mvn -B -f pom.xml -DskipTests dependency:go-offline

COPY . .
RUN mvn -B -DskipTests clean package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy the fat jar (adjust pattern if your artifactId/version differs)
COPY --from=build /workspace/target/*.jar app.jar
# Bind to port from env or default 8080
ENV JAVA_OPTS="-Xmx512m"
ENV PORT 8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT}"]
