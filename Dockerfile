FROM openjdk:8-jdk-alpine
MAINTAINER @RMaiun
VOLUME /tmp
COPY target/scala-2.12/*.jar tfbot.jar
EXPOSE 80 443
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /tfbot.jar ${@}"]