# stage 1 build
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# stage 2 runtime

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/simulation-1.0-jar-with-dependencies.jar simulation.jar

# katalog na pliki I/O
RUN mkdir /data

ENTRYPOINT ["java", "-jar", "/app/simulation.jar"]
CMD ["/data/input.json", "/data/output.json"]
