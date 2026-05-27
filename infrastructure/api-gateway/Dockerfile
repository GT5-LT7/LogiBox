FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd -r app && useradd -r -g app app
COPY build/libs/*-SNAPSHOT.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]