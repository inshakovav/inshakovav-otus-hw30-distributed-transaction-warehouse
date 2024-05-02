FROM openjdk:11
MAINTAINER otus-exeriments
#EXPOSE 8000/tcp
COPY target/*.jar app.jar
#ENTRYPOINT ["java","-jar","/app.jar"]
#ENTRYPOINT ["java", "-XX:InitialRAMPercentage=20","-XX:MaxRAMPercentage=30","-jar","/app.jar"]
ENTRYPOINT ["java", "-Xms512m","-Xmx3072m","-jar","/app.jar"]