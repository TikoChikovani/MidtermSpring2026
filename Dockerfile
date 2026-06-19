FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/uno-cli-1.0.0.jar app.jar

ENV UNO_DB_URL=jdbc:h2:file:/app/data/uno

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--bots", "3", "--games", "1"]
