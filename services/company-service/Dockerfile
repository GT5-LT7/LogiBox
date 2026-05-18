FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/company-service/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 19093
ENTRYPOINT ["java", "-jar", "app.jar"]