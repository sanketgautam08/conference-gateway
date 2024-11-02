FROM openjdk:21
ARG JAR_FILE=target/conference-gateway-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} conference.jar
ENTRYPOINT ["java", "-jar", "/conference.jar"]