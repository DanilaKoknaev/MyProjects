FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY startup-logger-spring-boot-starter/pom.xml startup-logger-spring-boot-starter/pom.xml
COPY finance-service/pom.xml finance-service/pom.xml
COPY startup-logger-spring-boot-starter/src startup-logger-spring-boot-starter/src
COPY finance-service/src finance-service/src
RUN mvn -B -Dmaven.test.skip=true -pl finance-service -am package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /workspace/finance-service/target/finance-service-*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
