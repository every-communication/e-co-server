FROM ubuntu:24.04

LABEL maintainer="rldnd <gi981226@gmail.com>"
LABEL description="e-co"
LABEL license="MIT"

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get clean && rm -rf /var/lib/apt/lists/* && apt-get update && \
    apt-get install -y locales tzdata curl zip unzip && \
    locale-gen en_US.UTF-8 && \
    update-locale LANG=en_US.UTF-8 LANGUAGE=en_US.UTF-8 LC_ALL=en_US.UTF-8 && \
    echo "Asia/Seoul" > /etc/timezone && \
    ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    dpkg-reconfigure --frontend noninteractive tzdata

RUN apt-get update && apt-get install -y openjdk-17-jdk
RUN curl -fsSL https://get.sdkman.io | bash && \
    bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install gradle && sdk install springboot"

WORKDIR /server
COPY src /server/src/
COPY gradle /server/gradle/
COPY gradlew /server/
COPY build.gradle /server/
COPY settings.gradle /server/

EXPOSE 8080

CMD ["./gradlew", "bootRun", "-Dspring.profiles.active=ci"]