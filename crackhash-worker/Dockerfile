FROM gradle:9.0.0-jdk21 AS build

WORKDIR /worker-service

COPY build.gradle .
COPY settings.gradle .

RUN gradle wrapper
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /worker-service/build/libs/*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]