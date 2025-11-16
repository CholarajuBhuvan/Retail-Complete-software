# ---------- build frontend ----------
FROM node:20-alpine AS webbuild
WORKDIR /app
COPY client/package*.json ./client/
WORKDIR /app/client
RUN npm ci || npm install
COPY client/ /app/client/
RUN npm run build

# ---------- build backend jar ----------
FROM maven:3.9.9-eclipse-temurin-21 AS backendbuild
WORKDIR /src
COPY billingsoftware /src/billingsoftware
WORKDIR /src/billingsoftware
RUN ./mvnw -q -DskipTests package

# ---------- runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=backendbuild /src/billingsoftware/target/*.jar /app/app.jar
COPY --from=webbuild /app/client/dist/ /app/public/
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]

