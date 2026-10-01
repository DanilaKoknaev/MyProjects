FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml ./
COPY order-service ./order-service
COPY payment-service ./payment-service
COPY shipping-service ./shipping-service
COPY notification-service ./notification-service
ARG MODULE=order-service
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl ${MODULE} -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG MODULE=order-service
COPY --from=build /workspace/${MODULE}/target/${MODULE}-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
