FROM openjdk:8-jdk-alpine
MAINTAINER @RMaiun
COPY target/scala-2.12/tfbot.jar /opt/tfbot.jar
ENV TOKEN="1111" CATAHOST="localhost" CATAPORT="9999"
EXPOSE 80 443
#ENTRYPOINT ["/usr/bin/java"]
CMD /usr/bin/java -jar -Dbot.token=$TOKEN -Dcataclysm.host=$CATAHOST -Dcataclysm.port=$CATAPORT  /opt/tfbot.jar

