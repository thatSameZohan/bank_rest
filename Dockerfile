FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

RUN ./gradlew build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/bank_rest-0.0.1-SNAPSHOT.jar"]

