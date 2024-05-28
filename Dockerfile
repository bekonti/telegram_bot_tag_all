FROM --platform=amd64 openjdk:21
MAINTAINER bekka811.kz
COPY target/*.jar bot.jar
ENTRYPOINT ["java","-jar","/bot.jar"]