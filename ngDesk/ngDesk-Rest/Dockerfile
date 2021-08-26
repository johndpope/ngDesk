FROM openjdk:11

EXPOSE 9080

RUN apt-get update
RUn apt-get install -y vim

COPY ./installbuilder-19.12.0/ /opt/installbuilder-19.12.0/

COPY ./nssm-2.24/ /opt/windows-files/nssm-2.24/

COPY ./controller /opt/linux-files/


COPY ./ngdesk-logo.png /opt/images/


COPY ./ngDesk-Controller-Updater.jar /opt/jars/
COPY ./ngDesk-Controller.jar /opt/jars/
COPY ./ngDesk-Uuid-Generator.jar /opt/jars/


RUN mkdir /opt/xml/
RUN mkdir /opt/installers/


COPY ./target/ngDesk-Rest-0.0.1-SNAPSHOT.jar /opt/

ENTRYPOINT ["sh", "-c", "java -jar /opt/ngDesk-Rest-0.0.1-SNAPSHOT.jar"]
# ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=devnew -jar /opt/ngDesk-Rest-0.0.1-SNAPSHOT.jar"]
