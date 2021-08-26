FROM openjdk:11

EXPOSE 8081

COPY ./target/ngDesk-Escalation-Service-0.0.1-SNAPSHOT.jar /opt/

ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=dev -jar /opt/ngDesk-Escalation-Service-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["ping", "127.0.0.1"]
