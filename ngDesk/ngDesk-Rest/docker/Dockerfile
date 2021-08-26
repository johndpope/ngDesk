ARG ENV_HOSTNAME
FROM $ENV_HOSTNAME:5000/java-base:1

COPY ./include/ngDesk-Rest-0.0.1-SNAPSHOT.jar /opt/
COPY ./include/elastic-apm-agent-1.5.0.jar /opt/

ARG track
ENV TRACK=${track}

ARG dc
ENV DC=${dc}

# ENTRYPOINT ["sh", "-c", "java -Dloader.path=lib -Dsun.net.inetaddr.ttl=0 -Dspring.profiles.active=$TRACK -jar /opt/ngDesk-Rest-0.0.1-SNAPSHOT.jar"]
ENTRYPOINT ["sh", "-c", "java -javaagent:/opt/elastic-apm-agent-1.5.0.jar -Delastic.apm.service_name=ngdesk-rest -Delastic.apm.server_url=http://$TRACK.apm-server.service.$DC.consul:8200 -Delastic.apm.application_packages=com.ngdesk -Dloader.path=lib -Dsun.net.inetaddr.ttl=0 -Dspring.profiles.active=$TRACK -jar /opt/ngDesk-Rest-0.0.1-SNAPSHOT.jar"]

