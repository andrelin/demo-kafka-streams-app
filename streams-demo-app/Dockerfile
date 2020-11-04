# syntax=docker/dockerfile:experimental
FROM amazoncorretto:11 as build
ARG CI

ADD gradlew .
ADD gradle gradle

# Project source
ADD src src
ADD .git .git
ADD build.gradle.kts .
ADD settings.gradle.kts .

# Gradle build
ENV CI=$CI
RUN --mount=type=cache,target=/root/.m2 --mount=type=cache,target=/root/.gradle \
    ./gradlew build --no-daemon --build-cache

# Application image
FROM amazoncorretto:11
COPY --from=build /build/libs/*.jar app.jar
ENTRYPOINT ["java",  "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
