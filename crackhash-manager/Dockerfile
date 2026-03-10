FROM gradle:8.6.0-jdk21 AS build

WORKDIR /manager-service

COPY build.gradle .
COPY settings.gradle .
#COPY gradle gradle
#COPY gradlew .

RUN gradle wrapper
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /manager-service/build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]