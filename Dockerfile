FROM openjdk:17-jdk-alpine
MAINTAINER azi.hassan@live.fr
COPY target/ticketing-0.0.2-SNAPSHOT.jar /
ENTRYPOINT ["java", "-jar", "ticketing-0.0.2-SNAPSHOT.jar"]
