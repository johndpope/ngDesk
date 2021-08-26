FROM openjdk:11
#FROM openjdk:8
# FROM alpine:3.8

# RUN apk update
# RUN apk fetch openjdk8-jre
# RUN apk add openjdk8-jre

EXPOSE 9081

COPY ./target/ngDesk-Manager-0.0.1-SNAPSHOT.jar /opt/

# ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=devnew -jar /opt/ngDesk-Manager-0.0.1-SNAPSHOT.jar"]
ENTRYPOINT ["sh", "-c", "java -jar /opt/ngDesk-Manager-0.0.1-SNAPSHOT.jar"]

