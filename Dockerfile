FROM eclipse-temurin:17

WORKDIR /app

ENTRYPOINT ["java", "-jar", "build/libs/spring-0.0.1-SNAPSHOT.jar"] 