# Stage 1: Build
FROM openjdk:17-jdk-slim AS build

WORKDIR /server
COPY src /server/src/
COPY gradle /server/gradle/
COPY gradlew /server/
COPY build.gradle /server/
COPY settings.gradle /server/

RUN bash -c "./gradlew build"

# Stage 2: Run
FROM openjdk:17-jdk-slim

LABEL maintainer="rldnd <gi981226@gmail.com>"
LABEL description="e-co"
LABEL license="MIT"

ENV DEBIAN_FRONTEND=noninteractive
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8

RUN apt-get update && apt-get install -y tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

WORKDIR /server
COPY --from=build /server/build/libs/*.jar /server/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/server/app.jar", "--spring.profiles.active=ci"]