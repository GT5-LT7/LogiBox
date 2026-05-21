FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/logistics-service/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 19094
ENTRYPOINT ["java", "-jar", "app.jar"]