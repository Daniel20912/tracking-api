# --- ETAPA 1: BUILD ---
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: RUNTIME ---
FROM eclipse-temurin:25-jre-alpine
LABEL authors="Daniel Oliveira"
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]