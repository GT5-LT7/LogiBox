FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY services/catalog-service/build/libs/*-SNAPSHOT.jar app.jar
RUN addgroup -S app && adduser -S app -G app
USER app
EXPOSE 19093
ENTRYPOINT ["java", "-jar", "app.jar"]