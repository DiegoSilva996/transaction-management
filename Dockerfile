FROM adoptopenjdk/openjdk11:alpine-jre
ARG JAR_FILE=target/*.jar 
COPY ${JAR_FILE} create_transaction.jar
ENTRYPOINT ["java", "-jar", "/create_transaction.jar"]