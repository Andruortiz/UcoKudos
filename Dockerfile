sFROM eclipse-temurin:27-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:27-jre
RUN useradd --system --uid 1001 app
USER app
WORKDIR /app
COPY --from=build /app/target/UcoKudos-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
