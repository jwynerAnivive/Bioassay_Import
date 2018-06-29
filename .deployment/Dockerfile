# Base Alpine Linux based image with OpenJDK JRE only
FROM openjdk:8-jre-alpine
WORKDIR /root/app
# copy application WAR (with libraries inside)
ADD /target/*.jar /root/app/app.jar
#ADD config.properties /root/app/config.properties
ADD log4j.properties /root/app/log4j.properties
# specify default command
#CMD ["/usr/bin/java", "-jar", "/app.jar"]