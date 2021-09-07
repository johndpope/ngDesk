FROM openjdk:11

EXPOSE 8888

COPY ./target/ngDesk-Config-Server-0.0.1-SNAPSHOT.jar /opt/

ENTRYPOINT ["java", "-jar", "/opt/ngDesk-Config-Server-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["ping", "127.0.0.1"]

