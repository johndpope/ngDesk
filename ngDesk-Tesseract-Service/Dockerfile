FROM openjdk:11

EXPOSE 8100

COPY ./target/ngDesk-Tesseract-Service-0.0.1-SNAPSHOT.jar /opt/

ENTRYPOINT ["sh", "-c", "java -jar /opt/ngDesk-Tesseract-Service-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=dev -jar /opt/ngDesk-Tesseract-Service-0.0.1-SNAPSHOT.jar"]
#ENTRYPOINT ["ping", "127.0.0.1"]
