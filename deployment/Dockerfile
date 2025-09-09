# ---- Etapa de construcción ----
FROM gradle:8.7-jdk21-alpine AS build
WORKDIR /home/gradle/app

COPY build.gradle settings.gradle gradle.* ./
COPY gradle ./gradle
COPY . .

RUN ./gradlew :app-service:clean :app-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Crear un usuario y grupo sin privilegios
RUN addgroup -S spring && adduser -S spring -G spring

# Ajustar permisos al directorio
RUN chown -R spring:spring /app

# Cambiar al usuario no root
USER spring:spring


ARG JAR_FILE=/home/gradle/app/applications/app-service/build/libs/*.jar

COPY --from=build ${JAR_FILE} app.jar

EXPOSE 8081

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} -jar /app/app.jar"]
