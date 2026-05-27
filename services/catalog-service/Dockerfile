FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY build/libs/*-SNAPSHOT.jar app.jar
USER app
EXPOSE 19093
ENTRYPOINT ["java", "-jar", "app.jar"]