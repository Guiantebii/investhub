# Build
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

COPY src src

RUN ./mvnw clean package -DskipTests -B

# Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --system --uid 1001 --home /app appuser
USER appuser

COPY --from=build /app/target/investhub-api-*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh

USER root
RUN chmod +x /docker-entrypoint.sh
USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["/docker-entrypoint.sh"]
