FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml checkstyle.xml ./
COPY src/main src/main

RUN sh ./mvnw --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system bookservice && useradd --system --gid bookservice bookservice

COPY --from=build /build/target/*.jar app.jar

USER bookservice

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
